package com.dev.example.httpclientwebclient.order.infrastructure.hermes.request

data class InvoiceCreatedEventDto(
    val invoiceId: String,
    val orderId: String,
    val timestamp: String
)
