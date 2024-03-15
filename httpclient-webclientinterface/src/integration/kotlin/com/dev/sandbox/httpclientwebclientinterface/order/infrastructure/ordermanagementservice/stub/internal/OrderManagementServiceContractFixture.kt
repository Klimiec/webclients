package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub.internal

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import java.util.UUID

object OrderManagementServiceContractFixture {

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

}
