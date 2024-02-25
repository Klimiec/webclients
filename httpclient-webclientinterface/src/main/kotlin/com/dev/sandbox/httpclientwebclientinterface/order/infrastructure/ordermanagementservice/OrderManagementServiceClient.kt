package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.OrdersDto
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.util.executeHttpRequest

class OrderManagementServiceClient(
    private val orderManagementServiceApi: OrderManagementServiceApi,
    private val clientName: String
) {
    suspend fun getOrdersFor(clientId: ClientId): OrdersDto {
        return executeHttpRequest(
            initialLog = "[$clientName] Get orders for a clientId= $clientId",
            request = { orderManagementServiceApi.getOrdersFor(clientId) },
            successLog = "[$clientName] Returned orders for a clientId= $clientId",
            failureMessage = "[$clientName] Failed to get orders for clientId= $clientId"
        )
    }
}
