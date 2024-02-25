package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes.request

data class InvoiceCreatedEventDto(
    val invoiceId: String,
    val orderId: String,
    val timestamp: String
)
