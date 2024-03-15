package com.dev.example.httpclientwebclient.order.domain

interface PublishInvoiceCreatedEvent {
    suspend fun publish(event: InvoiceCreated)
}
