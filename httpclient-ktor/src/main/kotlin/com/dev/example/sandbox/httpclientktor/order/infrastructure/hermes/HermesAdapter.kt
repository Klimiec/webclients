package com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientktor.order.domain.InvoiceCreated
import com.dev.example.sandbox.httpclientktor.order.domain.PublishInvoiceCreatedEvent
import com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes.request.mapToPublishInvoiceCreatedEventDto

class HermesAdapter(
    private val hermesClient: HermesClient
) : PublishInvoiceCreatedEvent {

    override suspend fun publish(event: InvoiceCreated) {
        event.mapToPublishInvoiceCreatedEventDto()
            .let { hermesClient.publish(event = it) }
    }
}
