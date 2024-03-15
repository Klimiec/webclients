package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes

import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.util.handleHttpResponse
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class HermesClient(
    private val hermesApi: HermesApi,
    private val clientName: String
) {
    suspend fun publish(event: InvoiceCreatedEventDto) {
        logger.info { "[$clientName] Publish InvoiceCreatedEvent $event" }
        handleHttpResponse(
            request = { hermesApi.publish(event) },
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event"
        ).also {
            logger.info { "[$clientName] Successfully published InvoiceCreatedEvent" }
        }
    }
}
