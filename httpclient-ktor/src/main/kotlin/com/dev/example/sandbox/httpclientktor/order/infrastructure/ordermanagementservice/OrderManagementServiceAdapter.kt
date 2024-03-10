package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response.toDomain
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceAdapter(private val orderManagementServiceClientKR: OrderManagementServiceClient) : GetOrderIds {
    override suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId> =
        orderManagementServiceClientKR.getOrdersFor(clientId)
            .toDomain()
            .also { logger.info { "OrderIds for clientId=$clientId = $it" } }
}
