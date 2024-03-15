package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes

import com.dev.sandbox.httpclientwebclientinterface.order.domain.InvoiceCreated
import com.dev.sandbox.httpclientwebclientinterface.order.domain.PublishInvoiceCreatedEvent
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes.request.mapToPublishInvoiceCreatedEventDto

class HermesAdapter(
    private val hermesClient: HermesClient
) : PublishInvoiceCreatedEvent {

    override suspend fun publish(event: InvoiceCreated) {
        event.mapToPublishInvoiceCreatedEventDto()
            .let { hermesClient.publish(event = it) }
    }
}
