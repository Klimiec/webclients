package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientretrofit.order.domain.ClientId
import com.dev.example.sandbox.httpclientretrofit.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientretrofit.order.domain.OrderId
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.response.toDomain
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceAdapter(private val orderManagementServiceClientRF: OrderManagementServiceClient) : GetOrderIds {
    override suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId> =
        orderManagementServiceClientRF.getOrdersFor(clientId)
            .toDomain()
            .also { logger.info { "OrderIds for clientId=$clientId = $it" } }
}
