package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes.request

data class InvoiceCreatedEventDto(
    val invoiceId: String,
    val orderId: String,
    val timestamp: String
)
