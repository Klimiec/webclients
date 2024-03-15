package com.dev.example.sandbox.httpclientktor.order.infrastructure.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache5.Apache5
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson

fun createHttpClient(properties: ConnectionProperties) =
    HttpClient(Apache5) {
        expectSuccess = true
        followRedirects = false

        defaultRequest {
            url(properties.baseUrl)
        }

        install(ContentNegotiation) {
            jackson(ContentType.Application.Json) {
            }
        }

        install(Logging) {
            level = LogLevel.ALL
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = properties.readTimeout
            connectTimeoutMillis = properties.connectionTimeout
            socketTimeoutMillis = properties.connectionTimeout
        }
    }

interface ConnectionProperties {
    var clientName: String
    var baseUrl: String
    var connectionTimeout: Long
    var readTimeout: Long
}
