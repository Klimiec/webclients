package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientretrofit.order.domain.InvoiceCreated
import com.dev.example.sandbox.httpclientretrofit.order.domain.PublishInvoiceCreatedEvent
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.request.mapToPublishInvoiceCreatedEventDto

class HermesAdapter(
    private val hermesClient: HermesClient
) : PublishInvoiceCreatedEvent {

    override suspend fun publish(event: InvoiceCreated) {
        event.mapToPublishInvoiceCreatedEventDto()
            .let { hermesClient.publish(event = it) }
    }
}
