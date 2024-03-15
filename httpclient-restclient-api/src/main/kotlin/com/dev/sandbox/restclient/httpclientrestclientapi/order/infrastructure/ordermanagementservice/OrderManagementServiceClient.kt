package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response.Order
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.handleHttpResponse
import mu.KotlinLogging
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

private val logger = KotlinLogging.logger {}

class OrderManagementServiceClient(
    private val restClient: RestClient,
    private val clientName: String
) {
    fun getOrdersFor(clientId: ClientId): List<Order> {
        logger.info { "[$clientName] Get orders for a clientId= ${clientId.clientId}" }
        return handleHttpResponse(
            request = restClient.get()
                .uri("/{clientId}/order") { uriBuilder -> uriBuilder.build(clientId.clientId.toString()) }
                .accept(MediaType.APPLICATION_JSON),
            failureMessage = "[$clientName] Failed to get orders for clientId=${clientId.clientId}",
            bodyType = object : ParameterizedTypeReference<List<Order>>() {}
        ).also {
            logger.info("[$clientName] Returned orders for a clientId= ${clientId.clientId} $it")
        }
    }
}
