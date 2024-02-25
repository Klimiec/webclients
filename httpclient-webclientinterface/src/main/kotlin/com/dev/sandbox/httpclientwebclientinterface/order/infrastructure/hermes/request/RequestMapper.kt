package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes.request

import com.dev.sandbox.httpclientwebclientinterface.order.domain.InvoiceCreated

fun InvoiceCreated.mapToPublishInvoiceCreatedEventDto() = InvoiceCreatedEventDto(
    invoiceId = invoiceId.invoiceId.toString(),
    orderId = orderId.orderId.toString(),
    timestamp = timestamp.toString()
)
