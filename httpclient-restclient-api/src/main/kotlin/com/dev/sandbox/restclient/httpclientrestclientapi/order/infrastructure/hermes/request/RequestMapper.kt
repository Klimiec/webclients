package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes.request

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.InvoiceCreated

fun InvoiceCreated.mapToPublishInvoiceCreatedEventDto() = InvoiceCreatedEventDto(
    invoiceId = invoiceId.invoiceId.toString(),
    orderId = orderId.orderId.toString(),
    timestamp = timestamp.toString()
)
