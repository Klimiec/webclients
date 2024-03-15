package com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice

import com.dev.example.httpclientwebclient.order.domain.ClientId
import com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice.response.Order
import com.dev.example.httpclientwebclient.order.infrastructure.util.handleHttpResponse
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

private val logger = KotlinLogging.logger {}

class OrderManagementServiceClient(
    private val webClient: WebClient,
    private val clientName: String
) {
    suspend fun getOrdersFor(clientId: ClientId): List<Order> {
        logger.info { "[$clientName] Get orders for a clientId= ${clientId.clientId}" }
        return handleHttpResponse(
            request = webClient.get()
                .uri("/{clientId}/order") { uriBuilder -> uriBuilder.build(clientId.clientId.toString()) }
                .accept(MediaType.APPLICATION_JSON),
            failureMessage = "[$clientName] Failed to get orders for clientId=${clientId.clientId}",
            monoType = object : ParameterizedTypeReference<List<Order>>() {}
        ).also {
            logger.info("[$clientName] Returned orders for a clientId= ${clientId.clientId} $it")
        }
    }
}
