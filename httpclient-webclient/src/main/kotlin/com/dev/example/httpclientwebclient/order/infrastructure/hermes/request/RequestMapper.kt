package com.dev.example.httpclientwebclient.order.infrastructure.hermes.request

import com.dev.example.httpclientwebclient.order.domain.InvoiceCreated

fun InvoiceCreated.mapToPublishInvoiceCreatedEventDto() = InvoiceCreatedEventDto(
    invoiceId = invoiceId.invoiceId.toString(),
    orderId = orderId.orderId.toString(),
    timestamp = timestamp.toString()
)
