package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.request

import com.dev.example.sandbox.httpclientretrofit.order.domain.InvoiceCreated

fun InvoiceCreated.mapToPublishInvoiceCreatedEventDto() = InvoiceCreatedEventDto(
    invoiceId = invoiceId.invoiceId.toString(),
    orderId = orderId.orderId.toString(),
    timestamp = timestamp.toString()
)
