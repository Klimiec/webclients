package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response

import com.dev.sandbox.httpclientwebclientinterface.order.domain.OrderId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ResponseMapperKtTest {

    @Test
    fun `should map order-management-service response to domain object`() {
        // given
        val rawResponse = sampleOrderManagementOrders()

        // when
        val orderIds = rawResponse.toDomain()

        // then
        orderIds.size shouldBe rawResponse.size
        orderIds[0] shouldBe OrderId.of(rawResponse[0].orderId)
        orderIds[1] shouldBe OrderId.of(rawResponse[1].orderId)
    }

    @Test
    fun `when server response contains incorrect values then throw exception`() {
        // given
        val rawResponse = sampleOrderManagementOrderWithIncorrectOrderIdValue()

        // expect
        shouldThrow<OrderManagementServiceResponseMappingException> {
            rawResponse.toDomain()
        }
    }
}

private fun sampleOrderManagementOrders(): List<Order> =
    listOf(
        Order(
            orderId = "7952a9ab-503c-4483-beca-32d081cc2388",
            categoryId = "327456",
            countryCode = "PL",
            clientId = "41323",
            price = Order.Price(amount = "1500", currency = "PLN")
        ),
        Order(
            orderId = "7952a9ab-503c-4483-beca-32d081cc2399",
            categoryId = "327477",
            countryCode = "CZ",
            clientId = "41322",
            price = Order.Price(amount = "999", currency = "CZK")
        )
    )

private fun sampleOrderManagementOrderWithIncorrectOrderIdValue(): List<Order> =
    listOf(
        Order(
            orderId = "7952a9ab-503c-4483", // not UUID
            categoryId = "327456",
            countryCode = "PL",
            clientId = "41323",
            price = Order.Price(amount = "1500", currency = "PLN")
        )
    )
