package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util

import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.util.Timeout
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient

fun createRestClient(restClientBuilder: RestClient.Builder, properties: ConnectionProperties) =
    restClientBuilder
        .requestFactory(requestFactory(properties))
        .baseUrl(properties.baseUrl)
        .defaultHeader(SERVICE_NAME, properties.clientName)
        .requestInterceptor(LoggingInterceptor(properties.clientName))
        .build()

private fun requestFactory(properties: ConnectionProperties): ClientHttpRequestFactory {
    val requestConfigBuilder = RequestConfig.custom()
        .setConnectionRequestTimeout(Timeout.ofMilliseconds(100))
        .setResponseTimeout(Timeout.ofMilliseconds(properties.readTimeout))
        .setRedirectsEnabled(false)

    val httpClient = HttpClientBuilder.create()
        .disableAutomaticRetries()
        .disableCookieManagement()
        .disableRedirectHandling()
        .disableContentCompression()
//        .setConnectionManager()
        .setDefaultRequestConfig(requestConfigBuilder.build()).build()

    return HttpComponentsClientHttpRequestFactory(httpClient)
}

interface ConnectionProperties {
    var clientName: String
    var baseUrl: String
    var connectionTimeout: Int
    var readTimeout: Long
}
