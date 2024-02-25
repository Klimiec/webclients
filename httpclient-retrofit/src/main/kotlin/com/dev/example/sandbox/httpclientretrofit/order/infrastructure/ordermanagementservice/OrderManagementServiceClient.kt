package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientretrofit.order.domain.ClientId
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.response.OrdersDto
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.handleHttpResponse

class OrderManagementServiceClient(
    private val orderManagementServiceApi: OrderManagementServiceApi,
    private val clientName: String
) {
    suspend fun getOrdersFor(clientId: ClientId): OrdersDto {
        return handleHttpResponse(
            initialLog = "[$clientName] Get orders for a clientId= $clientId",
            request = { orderManagementServiceApi.getOrdersFor(clientId) },
            successLog = "[$clientName] Returned orders for a clientId= $clientId",
            failureMessage = "[$clientName] Failed to get orders for clientId=$clientId"
        )
    }
}
