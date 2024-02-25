package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.stub.external

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response.Order
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response.OrdersDto
import java.util.UUID

object OrderManagementServiceFixture {

    fun ordersPlacedBySomeCustomer(
        orderId: String = "7952a9ab-503c-4483-beca-32d081cc2446"
    ) = OrdersDto(listOf(Order(orderId)))

    fun anyClientId() = ClientId(UUID.randomUUID())
}
