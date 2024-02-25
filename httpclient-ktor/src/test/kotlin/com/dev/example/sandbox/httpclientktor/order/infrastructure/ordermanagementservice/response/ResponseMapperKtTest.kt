package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response

import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
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
        orderIds.size shouldBe rawResponse.orders.size
        orderIds[0] shouldBe OrderId.of(rawResponse.orders[0].orderId)
        orderIds[1] shouldBe OrderId.of(rawResponse.orders[1].orderId)
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

private fun sampleOrderManagementOrders() =
    OrdersDto(
        listOf(
            Order(
                orderId = "7952a9ab-503c-4483-beca-32d081cc2388"
            ),
            Order(
                orderId = "7952a9ab-503c-4483-beca-32d081cc2399"
            )
        )
    )

private fun sampleOrderManagementOrderWithIncorrectOrderIdValue() =
    OrdersDto(
        listOf(
            Order(
                orderId = "7952a9ab-503c-4483" // not UUID
            )
        )
    )
