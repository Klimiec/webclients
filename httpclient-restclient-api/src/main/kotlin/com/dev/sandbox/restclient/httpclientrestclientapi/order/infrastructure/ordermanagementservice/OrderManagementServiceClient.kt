package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response.OrdersDto
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.executeHttpRequest
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

class OrderManagementServiceClient(
    private val restClient: RestClient,
    private val clientName: String
) {
    fun getOrdersFor(clientId: ClientId): OrdersDto {
        return executeHttpRequest(
            initialLog = "[$clientName] Get orders for a clientId= $clientId",
            request = restClient.get()
                .uri("/{clientId}/order") { uriBuilder -> uriBuilder.build(clientId) }
                .accept(MediaType.APPLICATION_JSON),
            successLog = "[$clientName] Returned orders for a clientId= $clientId",
            failureMessage = "[$clientName] Failed to get orders for clientId=$clientId",
            bodyType = OrdersDto::class.java
        )
    }
}
