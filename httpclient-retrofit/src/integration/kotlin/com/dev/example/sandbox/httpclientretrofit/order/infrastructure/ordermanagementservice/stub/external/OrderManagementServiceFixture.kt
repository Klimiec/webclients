package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.stub.external

import com.dev.example.sandbox.httpclientretrofit.order.domain.ClientId
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.response.Order
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.response.OrdersDto
import java.util.UUID

object OrderManagementServiceFixture {

    fun ordersPlacedBySomeCustomer(
        orderId: String = "7952a9ab-503c-4483-beca-32d081cc2446"
    ) = OrdersDto(listOf(Order(orderId)))

    fun anyClientId() = ClientId(UUID.randomUUID())
}
