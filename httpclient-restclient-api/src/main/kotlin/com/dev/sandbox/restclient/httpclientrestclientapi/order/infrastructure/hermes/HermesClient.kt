package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes

import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.executeHttpRequestNoResponse
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class HermesClient(
    private val restClient: RestClient,
    private val clientName: String
) {
    fun publish(event: InvoiceCreatedEventDto) {
        executeHttpRequestNoResponse(
            initialLog = "[$clientName] Publish InvoiceCreatedEvent $event",
            request = restClient.post()
                .uri("/topics/topic-invoice-created")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event),
            successLog = "[$clientName] Successfully published InvoiceCreatedEvent",
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event"
        )
    }
}
