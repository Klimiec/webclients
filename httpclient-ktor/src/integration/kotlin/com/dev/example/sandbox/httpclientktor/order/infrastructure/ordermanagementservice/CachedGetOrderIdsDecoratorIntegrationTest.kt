package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.BaseIntegrationTest
import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceFixture.anyClientId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceFixture.ordersPlacedBySomeCustomer
import com.github.benmanes.caffeine.cache.Cache
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["services.order-management-service.cache.enabled = true"])
@WireMockTest(httpPort = 8082)
internal class CachedGetOrderIdsDecoratorIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var cachedOrderManagementServiceAdapter: GetOrderIds

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @Autowired
    lateinit var cacheProperties: OrderManagementServiceCacheProperties

    lateinit var cache: Cache<ClientId, List<OrderId>>

    @BeforeEach
    fun setUp() {
        cache = (cachedOrderManagementServiceAdapter as CachedGetOrderIdsDecorator).getCache()
    }

    @Test
    fun `should not call external service when value is already present in the cache`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stub.orderManagementService().willReturnOrdersFor(clientId, response = ordersPlacedBySomeCustomer())

        // and: first call for OrderIds with "clientId" should populate cache
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientId)

        // when: call for OrderIds with "clientId" once again
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientId)

        // then: second call for OrderIds with "clientId" should not call external service
        stub.orderManagementService().verifyGetOrdersCalled(count = 1, clientId)

        // and: there is value in cache for key "clientId"
        val cachedOrderIds = cache.getIfPresent(clientId)!!
        cachedOrderIds.size shouldBe 1
        cachedOrderIds[0] shouldBe OrderId.of("7952a9ab-503c-4483-beca-32d081cc2446")
    }

    @Test
    fun `should evict cache when size limit of the cache is exceeded`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stub.orderManagementService().willReturnOrdersFor(clientId, response = ordersPlacedBySomeCustomer())
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientId)
        // and: populate cache to exceed the threshold by 1
        repeat(cacheProperties.size.toInt()) {
            val someClientId = anyClientId()
            stub.orderManagementService().willReturnOrdersFor(someClientId, response = ordersPlacedBySomeCustomer())
            cachedOrderManagementServiceAdapter.getOrderIdsFor(someClientId)
        }
        // when: await the completion of the cache eviction
        cache.cleanUp()
        // then
        cache.getIfPresent(clientId) shouldBe null
    }

    @Test
    fun `should remove entry from cache after threshold period is passed since the last write`(): Unit = runBlocking {
        // given
        val clientIdA = anyClientId()
        stub.orderManagementService().willReturnOrdersFor(clientIdA, response = ordersPlacedBySomeCustomer())
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientIdA)

        // when
        Thread.sleep(cacheProperties.expireAfter.toMillis())

        // and: await the completion of the eviction
        cache.cleanUp()

        // then
        cache.getIfPresent(clientIdA) shouldBe null
    }

    @Test
    fun `verify cache metrics contain cache name as a tag`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stub.orderManagementService().willReturnOrdersFor(clientId, response = ordersPlacedBySomeCustomer())

        // when
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientId)

        // then
        meterRegistry.get("cache.loads").tags("cache", cacheProperties.name).timer().count() shouldBeGreaterThan 0
    }

    @AfterEach
    fun tearDown() {
        cache.invalidateAll()
    }
}
