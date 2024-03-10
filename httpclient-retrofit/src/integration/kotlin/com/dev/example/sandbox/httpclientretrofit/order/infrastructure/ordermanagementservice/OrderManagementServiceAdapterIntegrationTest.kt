package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientretrofit.BaseIntegrationTest
import com.dev.example.sandbox.httpclientretrofit.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientretrofit.order.domain.OrderId
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.stub.OrderCoreServiceFixture
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@WireMockTest(httpPort = 8082)
class OrderManagementServiceAdapterIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var orderCoreServiceAdapter: GetOrderIds

    @Test
    fun `should return orderIds for a given clientId`(): Unit = runBlocking {
        // given
        val clientId = OrderCoreServiceFixture.anyClientId()
        val ordersPlacedByPolishCustomer = OrderCoreServiceFixture.ordersPlacedByPolishCustomer(clientId = clientId.clientId.toString())
        stubs.orderManagementService().willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer)

        // when
        val orderIds = orderCoreServiceAdapter.getOrderIdsFor(clientId)

        // then
        orderIds shouldHaveSize ordersPlacedByPolishCustomer.size
        orderIds[0] shouldBe OrderId.of(ordersPlacedByPolishCustomer[0].orderId)
    }
}
