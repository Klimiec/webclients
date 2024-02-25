package com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice

import com.dev.example.httpclientwebclient.order.domain.ClientId
import com.dev.example.httpclientwebclient.order.domain.GetOrderIds
import com.dev.example.httpclientwebclient.order.domain.OrderId
import com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice.response.toDomain
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceAdapter(private val orderManagementServiceClient: OrderManagementServiceClient) : GetOrderIds {
    override suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId> =
        orderManagementServiceClient.getOrdersFor(clientId)
            .toDomain()
            .also { logger.info { "OrderIds for clientId= $clientId  $it" } }
}
