package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.stub.external

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response.Order
import java.util.UUID

object OrderManagementServiceFixture {

    fun ordersPlacedByPolishCustomer(
        orderId: String = "7952a9ab-503c-4483-beca-32d081cc2446",
        categoryId: String = "327456",
        clientId: String = "1a575762-0903-4b7a-9da3-d132f487c5ae",
        price: Order.Price = Order.Price(
            amount = "1500",
            currency = "PLN"
        )
    ) = listOf(Order(orderId, categoryId, "PL", clientId, price))

    fun anyClientId() = ClientId(UUID.randomUUID())
}
