package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes

import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.util.executeHttpRequest

class HermesClient(
    private val hermesApi: HermesApi,
    private val clientName: String
) {
    suspend fun publish(event: InvoiceCreatedEventDto) {
        executeHttpRequest(
            initialLog = "[$clientName] Publish InvoiceCreatedEvent $event",
            request = { hermesApi.publish(event) },
            successLog = "[$clientName] Successfully published InvoiceCreatedEvent",
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event"
        )
    }
}
