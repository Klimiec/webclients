package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientretrofit.order.domain.ClientId
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.response.Order
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.handleHttpResponse
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceClient(
    private val orderManagementServiceApi: OrderManagementServiceApi,
    private val clientName: String
) {
    suspend fun getOrdersFor(clientId: ClientId): List<Order> {
        logger.info { "[$clientName] Get orders for a clientId= ${clientId.clientId}" }
        return handleHttpResponse(
            response = orderManagementServiceApi.getOrdersFor(clientId.clientId.toString()),
            failureMessage = "[$clientName] Failed to get orders for clientId=${clientId.clientId}"
        ).also {
            logger.info("[$clientName] Returned orders for a clientId= ${clientId.clientId} $it")
        }
    }
}
