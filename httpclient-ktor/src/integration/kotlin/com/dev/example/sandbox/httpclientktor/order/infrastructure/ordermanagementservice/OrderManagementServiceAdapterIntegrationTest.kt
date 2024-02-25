package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.BaseIntegrationTest
import com.dev.example.sandbox.httpclientktor.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceFixture.anyClientId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceFixture.ordersPlacedBySomeCustomer
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@WireMockTest(httpPort = 8082)
class OrderManagementServiceAdapterIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var orderManagementServiceAdapter: GetOrderIds

    @Test
    fun `should return orderIds for a given clientId`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        val orderManagementResponse = ordersPlacedBySomeCustomer()
        stub.orderManagementService().willReturnOrdersFor(clientId, response = orderManagementResponse)

        // when
        val orderIds = orderManagementServiceAdapter.getOrderIdsFor(clientId)

        // then
        orderIds shouldHaveSize orderManagementResponse.orders.size
        orderIds[0] shouldBe OrderId.of(orderManagementResponse.orders[0].orderId)
    }
}
