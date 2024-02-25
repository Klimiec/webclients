package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.util

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import mu.KotlinLogging
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

inline fun <reified T> createExternalServiceApi(
    webClientBuilder: WebClient.Builder,
    properties: ConnectionProperties
): T =
    webClientBuilder
        .clientConnector(httpClient(properties))
        .baseUrl(properties.baseUrl)
        .defaultRequest { it.attribute(SERVICE_NAME, properties.clientName) }
        .filter(logRequestInfo(properties.clientName))
        .filter(logResponseInfo(properties.clientName))
        .build()
        .let { WebClientAdapter.create(it) }
        .let { HttpServiceProxyFactory.builderFor(it).build() }
        .createClient(T::class.java)

fun httpClient(properties: ConnectionProperties) = ReactorClientHttpConnector(
    HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectionTimeout)
        .doOnConnected {
            it.addHandlerLast(ReadTimeoutHandler(properties.readTimeout, TimeUnit.MILLISECONDS))
            it.addHandlerLast(WriteTimeoutHandler(3000, TimeUnit.MILLISECONDS))
        }
)

fun logRequestInfo(clientName: String) = ExchangeFilterFunction.ofRequestProcessor { request ->
    logger.info {
        "[$clientName] method=[${request.method().name()}] url=${request.url()}}"
    }
    Mono.just(request)
}

fun logResponseInfo(clientName: String) = ExchangeFilterFunction.ofResponseProcessor { response ->
    logger.info { "[$clientName] service responded with a status code= ${response.statusCode()}" }
    Mono.just(response)
}

interface ConnectionProperties {
    var clientName: String
    var baseUrl: String
    var connectionTimeout: Int
    var readTimeout: Long
}
