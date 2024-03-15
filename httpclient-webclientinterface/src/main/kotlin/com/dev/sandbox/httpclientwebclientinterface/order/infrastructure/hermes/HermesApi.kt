package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes

import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

interface HermesApi {
    @PostExchange(url = "/topics/topic-invoice-created", contentType = MediaType.APPLICATION_JSON_VALUE)
    suspend fun publish(@RequestBody event: InvoiceCreatedEventDto)
}
