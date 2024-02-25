package com.dev.example.httpclientwebclient.order.infrastructure.util

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

fun createWebClient(webClientBuilder: WebClient.Builder, properties: ConnectionProperties): WebClient {
    return webClientBuilder
        .clientConnector(httpClient(properties))
        .baseUrl(properties.baseUrl)
        .defaultRequest { it.attribute(SERVICE_NAME, properties.clientName) }
        .filter(logRequestInfo(properties.clientName))
        .filter(logResponseInfo(properties.clientName))
        .build()
}

private fun httpClient(properties: ConnectionProperties): ReactorClientHttpConnector {
    return ReactorClientHttpConnector(
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectionTimeout)
            .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(properties.readTimeout, TimeUnit.MILLISECONDS))
                it.addHandlerLast(WriteTimeoutHandler(3000, TimeUnit.MILLISECONDS))
            }
    )
}

private fun logRequestInfo(clientName: String) = ExchangeFilterFunction.ofRequestProcessor { request ->
    webLogger.info {
        "[$clientName] method=[${request.method().name()}] url=${request.url()}"
    }
    Mono.just(request)
}

private fun logResponseInfo(clientName: String) = ExchangeFilterFunction.ofResponseProcessor { response ->
    webLogger.info { "[$clientName] service responded with a status code= ${response.statusCode()}" }
    Mono.just(response)
}

interface ConnectionProperties {
    var clientName: String
    var baseUrl: String
    var connectionTimeout: Int
    var readTimeout: Long
}
