package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.Order
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.util.handleHttpResponseAsList
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceClient(
    private val orderManagementServiceApi: OrderManagementServiceApi,
    private val clientName: String,
) {
    suspend fun getOrdersFor(clientId: ClientId): List<Order> {
        logger.info { "[$clientName] Get orders for a clientId= ${clientId.clientId}" }
        return handleHttpResponseAsList(
            request = { orderManagementServiceApi.getOrdersFor(clientId.clientId.toString()) },
            failureMessage = "[$clientName] Failed to get orders for clientId=${clientId.clientId}"
        ).also {
            logger.info { "[$clientName] Returned orders for a clientId= ${clientId.clientId} $it" }
        }
    }
}
