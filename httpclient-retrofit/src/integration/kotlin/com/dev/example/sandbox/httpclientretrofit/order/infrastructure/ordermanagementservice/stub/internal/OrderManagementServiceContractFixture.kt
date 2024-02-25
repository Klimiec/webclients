package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.stub.internal

object OrderManagementServiceContractFixture {

    fun ordersPlacedBySomeCustomer() = """
{
    "orders": [
        {
            "orderId": "7952a9ab-503c-4483-beca-32d081cc2446"
        }
    ]
}

    """.trimIndent()
}
