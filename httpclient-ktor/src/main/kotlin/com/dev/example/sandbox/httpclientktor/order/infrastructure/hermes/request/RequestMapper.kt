package com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes.request

import com.dev.example.sandbox.httpclientktor.order.domain.InvoiceCreated

fun InvoiceCreated.mapToPublishInvoiceCreatedEventDto() = InvoiceCreatedEventDto(
    invoiceId = invoiceId.invoiceId.toString(),
    orderId = orderId.orderId.toString(),
    timestamp = timestamp.toString()
)
