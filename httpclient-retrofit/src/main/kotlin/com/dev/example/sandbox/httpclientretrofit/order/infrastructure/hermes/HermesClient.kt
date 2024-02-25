package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.handleHttpResponse

class HermesClient(
    private val hermesApi: HermesApi,
    private val clientName: String
) {
    suspend fun publish(event: InvoiceCreatedEventDto) {
        return handleHttpResponse(
            initialLog = "[$clientName] Publish InvoiceCreatedEvent $event",
            request = { hermesApi.publish(event) },
            successLog = "[$clientName] Successfully published InvoiceCreatedEvent",
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event"
        )
    }
}
