package com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes.request

data class InvoiceCreatedEventDto(
    val invoiceId: String,
    val orderId: String,
    val timestamp: String
)
