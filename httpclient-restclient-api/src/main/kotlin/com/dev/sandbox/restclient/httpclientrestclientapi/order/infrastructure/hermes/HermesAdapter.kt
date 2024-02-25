package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.InvoiceCreated
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.PublishInvoiceCreatedEvent
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes.request.mapToPublishInvoiceCreatedEventDto

class HermesAdapter(
    private val hermesClient: HermesClient
) : PublishInvoiceCreatedEvent {
    override fun publish(event: InvoiceCreated) {
        event.mapToPublishInvoiceCreatedEventDto()
            .let { hermesClient.publish(event = it) }
    }
}
