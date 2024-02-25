package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub.external

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.OrderDto
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.OrdersDto
import java.util.UUID

object OrderManagementServiceFixture {

    fun ordersPlacedBySomeCustomer(
        orderId: String = "7952a9ab-503c-4483-beca-32d081cc2446"
    ) = OrdersDto(listOf(OrderDto(orderId)))

    fun anyClientId() = ClientId(UUID.randomUUID())
}
