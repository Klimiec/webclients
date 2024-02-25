package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response.OrdersDto
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.executeHttpRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.takeFrom

class OrderManagementServiceClient(
    private val httpClient: HttpClient,
    private val clientName: String
) {
    suspend fun getOrdersFor(clientId: ClientId): OrdersDto {
        return executeHttpRequest(
            initialLog = "[$clientName] Get orders for a clientId= $clientId",
            request = {
                httpClient.get {
                    url.takeFrom("/${clientId}/order")
                    accept(ContentType.Application.Json)
                }.body()
            },
            successLog = "[$clientName] Returned orders for a clientId= $clientId",
            failureMessage = "[$clientName] Failed to get orders for clientId=$clientId"
        )
    }
}
