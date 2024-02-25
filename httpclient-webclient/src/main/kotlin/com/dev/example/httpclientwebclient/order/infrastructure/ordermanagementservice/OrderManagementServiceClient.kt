package com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice

import com.dev.example.httpclientwebclient.order.domain.ClientId
import com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice.response.OrdersDto
import com.dev.example.httpclientwebclient.order.infrastructure.util.executeHttpRequest
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

class OrderManagementServiceClient(
    private val webClient: WebClient,
    private val clientName: String
) {
    suspend fun getOrdersFor(clientId: ClientId): OrdersDto {
        return executeHttpRequest(
            initialLog = "[$clientName] Get orders for a clientId= $clientId",
            request = webClient.get()
                .uri("/{clientId}/order") { uriBuilder -> uriBuilder.build(clientId) }
                .accept(MediaType.APPLICATION_JSON),
            successLog = "[$clientName] Returned orders for a clientId= $clientId",
            failureMessage = "[$clientName] Failed to get orders for clientId=$clientId"
        )
    }
}
