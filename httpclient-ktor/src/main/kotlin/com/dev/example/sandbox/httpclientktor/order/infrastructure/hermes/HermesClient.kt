package com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.executeHttpRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom

class HermesClient(
    private val httpClient: HttpClient,
    private val clientName: String
) {
    suspend fun publish(event: InvoiceCreatedEventDto) {
        executeHttpRequest(
            initialLog = "[$clientName] Publish InvoiceCreatedEvent $event",
            request = {
                httpClient.post {
                    url.takeFrom("/topics/topic-invoice-created")
                    contentType(ContentType.Application.Json)
                    setBody(event)
                }
            },
            successLog = "[$clientName] Successfully published InvoiceCreatedEvent",
            failureMessage = "[$clientName] Failed to publish InvoiceCreatedEvent $event"
        )
    }
}
