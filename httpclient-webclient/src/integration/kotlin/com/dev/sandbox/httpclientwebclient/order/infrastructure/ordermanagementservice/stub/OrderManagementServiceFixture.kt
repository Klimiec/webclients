package com.dev.sandbox.httpclientwebclient.order.infrastructure.ordermanagementservice.stub

import com.dev.example.httpclientwebclient.order.domain.ClientId
import com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice.response.Order
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

    fun ordersPlacedByPolishCustomer() = """
        [
          {
            "orderId": "7952a9ab-503c-4483-beca-32d081cc2446",
            "categoryId": "327456",
            "countryCode": "PL",
            "clientId": "1a575762-0903-4b7a-9da3-d132f487c5ae",
            "price": {
              "amount": "1500",
              "currency": "PLN"
            }
          }
        ]
    """.trimIndent()

    fun anyClientId() = ClientId(UUID.randomUUID())
}
