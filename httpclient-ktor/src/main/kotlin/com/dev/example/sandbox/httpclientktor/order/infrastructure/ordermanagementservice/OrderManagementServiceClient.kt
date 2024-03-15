package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response.Order
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.handleHttpResponseAsList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.takeFrom
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceClient(
    private val httpClient: HttpClient,
    private val clientName: String
) {
    suspend fun getOrdersFor(clientId: ClientId): List<Order> {
        logger.info { "[$clientName] Get orders for a clientId= ${clientId.clientId}" }
        return handleHttpResponseAsList(
            request = {
                httpClient.get {
                    url.takeFrom("/${clientId.clientId}/order")
                    accept(ContentType.Application.Json)
                }.body()
            },
            failureMessage = "[$clientName] Failed to get orders for clientId=${clientId.clientId}"
        )
    }
}
