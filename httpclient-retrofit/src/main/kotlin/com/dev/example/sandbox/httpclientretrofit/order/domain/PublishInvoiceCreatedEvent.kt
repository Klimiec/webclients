package com.dev.example.sandbox.httpclientretrofit.order.domain

interface PublishInvoiceCreatedEvent {
    suspend fun publish(event: InvoiceCreated)
}
