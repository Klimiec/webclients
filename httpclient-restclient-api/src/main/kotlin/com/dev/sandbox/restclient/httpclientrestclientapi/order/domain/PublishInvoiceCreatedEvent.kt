package com.dev.sandbox.restclient.httpclientrestclientapi.order.domain

interface PublishInvoiceCreatedEvent {
    fun publish(event: InvoiceCreated)
}
