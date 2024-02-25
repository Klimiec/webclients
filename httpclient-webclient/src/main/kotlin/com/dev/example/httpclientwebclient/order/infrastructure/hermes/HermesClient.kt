package com.dev.example.httpclientwebclient.order.infrastructure.hermes

import com.dev.example.httpclientwebclient.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.example.httpclientwebclient.order.infrastructure.util.executeHttpRequestNoResponse
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class HermesClient(
    private val webClient: WebClient,
    private val clientName: String
) {
    suspend fun publish(event: InvoiceCreatedEventDto) {
        executeHttpRequestNoResponse(
            initialLog = "[$clientName] Publish InvoiceCreatedEvent $event",
            request = webClient.post()
                .uri("/topics/topic-invoice-created")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(event),
            successLog = "[$clientName] Successfully published InvoiceCreatedEvent",
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event"
        )
    }
}
