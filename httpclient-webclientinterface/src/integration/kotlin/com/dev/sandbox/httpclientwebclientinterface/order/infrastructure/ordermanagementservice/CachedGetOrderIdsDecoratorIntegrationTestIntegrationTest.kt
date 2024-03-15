package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice

import com.dev.sandbox.httpclientwebclientinterface.BaseIntegrationTest
import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.domain.GetOrderIds
import com.dev.sandbox.httpclientwebclientinterface.order.domain.OrderId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceFixture.anyClientId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceFixture.ordersPlacedByPolishCustomer
import com.github.benmanes.caffeine.cache.Cache
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ["services.order-management-service.cache.enabled = true"])
@WireMockTest(httpPort = 8082)
internal class CachedGetOrderIdsDecoratorIntegrationTestIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var cachedOrderManagementServiceAdapter: GetOrderIds

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @Autowired
    lateinit var cacheProperties: OrderManagementServiceCacheProperties

    lateinit var cache: Cache<ClientId, List<OrderId>>

    @BeforeEach
    fun setUp() {
        cache = (cachedOrderManagementServiceAdapter as CachedGetOrderIdsDecoratorIntegrationTest).getCache()
    }

    @Test
    fun `should not call external service when value is already present in the cache`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stubs.orderManagementService().willReturnOrdersFor(
            clientId,
            response = ordersPlacedByPolishCustomer(clientId = clientId.toString())
        )

        // and: first call for OrderIds with "clientId" should populate cache
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientId)

        // when: call for OrderIds with "clientId" once again
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientId)

        // then: second call for OrderIds with "clientId" should not call external service
        stubs.orderManagementService().verifyGetOrdersCalled(count = 1, clientId)

        // and: there is value in cache for key "clientId"
        cache.getIfPresent(clientId) shouldNotBe null
    }

    @Test
    fun `should evict cache when size limit of the cache is exceeded`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stubs.orderManagementService().willReturnOrdersFor(
            clientId,
            response = ordersPlacedByPolishCustomer(clientId = clientId.toString())
        )
        cachedOrderManagementServiceAdapter.getOrderIdsFor(clientId)
        // and: populate cache to exceed the threshold by 1
        repeat(cacheProperties.size.toInt()) {
            val someClientId = anyClientId()
            stubs.orderManagementService().willReturnOrdersFor(
                someClientId,
                response = ordersPlacedByPolishCustomer(clientId = someClientId.toString())
            )
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
        stubs.orderManagementService().willReturnOrdersFor(
            clientIdA,
            response = ordersPlacedByPolishCustomer(clientId = clientIdA.toString())
        )
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
        stubs.orderManagementService().willReturnOrdersFor(
            clientId,
            response = ordersPlacedByPolishCustomer(clientId = clientId.toString())
        )

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
