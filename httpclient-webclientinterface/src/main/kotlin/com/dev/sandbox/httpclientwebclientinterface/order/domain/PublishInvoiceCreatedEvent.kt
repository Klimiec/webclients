package com.dev.sandbox.httpclientwebclientinterface.order.domain

interface PublishInvoiceCreatedEvent {
    suspend fun publish(event: InvoiceCreated)
}
