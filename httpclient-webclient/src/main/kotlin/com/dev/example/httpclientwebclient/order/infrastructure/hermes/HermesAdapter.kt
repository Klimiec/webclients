package com.dev.example.httpclientwebclient.order.infrastructure.hermes

import com.dev.example.httpclientwebclient.order.domain.InvoiceCreated
import com.dev.example.httpclientwebclient.order.domain.PublishInvoiceCreatedEvent
import com.dev.example.httpclientwebclient.order.infrastructure.hermes.request.mapToPublishInvoiceCreatedEventDto

class HermesAdapter(
    private val hermesClient: HermesClient
) : PublishInvoiceCreatedEvent {

    override suspend fun publish(event: InvoiceCreated) {
        event.mapToPublishInvoiceCreatedEventDto()
            .let { hermesClient.publish(event = it) }
    }
}
