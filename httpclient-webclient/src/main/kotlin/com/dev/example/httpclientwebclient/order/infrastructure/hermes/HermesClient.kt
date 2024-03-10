package com.dev.example.httpclientwebclient.order.infrastructure.hermes

import com.dev.example.httpclientwebclient.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.example.httpclientwebclient.order.infrastructure.util.handleHttpResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

class HermesClient(
    private val webClient: WebClient,
    private val clientName: String,
) {
    suspend fun publish(event: InvoiceCreatedEventDto) {
        logger.info { "[$clientName] Publish InvoiceCreatedEvent $event" }
        handleHttpResponse(
            request = webClient.post()
                .uri("/topics/topic-invoice-created")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(event),
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event"
        ).also {
            logger.info { "[$clientName] Successfully published InvoiceCreatedEvent" }
        }
    }
}