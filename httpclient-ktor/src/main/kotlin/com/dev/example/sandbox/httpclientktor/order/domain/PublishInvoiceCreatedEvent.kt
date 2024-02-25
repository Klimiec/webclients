package com.dev.example.sandbox.httpclientktor.order.domain

interface PublishInvoiceCreatedEvent {
    suspend fun publish(event: InvoiceCreated)
}
