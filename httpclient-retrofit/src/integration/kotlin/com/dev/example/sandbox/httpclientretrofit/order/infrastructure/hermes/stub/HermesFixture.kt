package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.stub

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.request.InvoiceCreatedEventDto

object HermesFixture {
    fun invoiceCreatedEvent() = InvoiceCreatedEventDto(
        invoiceId = "3cc58510-c767-4cc4-8e67-ae1adf201ead",
        orderId = "63150dfe-2044-472b-a579-f2f2977675d2",
        timestamp = "2024-02-04T15:27:39.301808Z"
    )
}
