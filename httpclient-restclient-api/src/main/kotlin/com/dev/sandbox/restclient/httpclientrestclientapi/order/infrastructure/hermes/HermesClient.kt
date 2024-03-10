package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes

import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.handleHttpResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

private val logger = KotlinLogging.logger {}

class HermesClient(
    private val restClient: RestClient,
    private val clientName: String,
) {
    fun publish(event: InvoiceCreatedEventDto) {
        logger.info { "[$clientName] Publish InvoiceCreatedEvent $event" }
        handleHttpResponse(
            request = restClient.post()
                .uri("/topics/topic-invoice-created")
                .contentType(MediaType.APPLICATION_JSON)
                .body(event),
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event",
        ).also {
            logger.info { "[$clientName] Successfully published InvoiceCreatedEvent" }
        }
    }
}