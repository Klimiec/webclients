package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util

import mu.KotlinLogging
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

private val logger = KotlinLogging.logger {}

class LoggingInterceptor(private val clientName: String) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        logger.info { "[$clientName] method=[${request.method}] url=${request.uri}" }
        val response = execution.execute(request, body)
        logger.info { "[$clientName] service responded with a status code= ${response.statusCode}" }
        return response
    }
}
