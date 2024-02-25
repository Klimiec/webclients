package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.domain.GetOrderIds
import com.dev.sandbox.httpclientwebclientinterface.order.domain.OrderId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.toDomain
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceAdapter(private val orderManagementServiceClient: OrderManagementServiceClient) :
    GetOrderIds {
    override suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId> =
        orderManagementServiceClient.getOrdersFor(clientId)
            .toDomain()
            .also { logger.info { "Mapped to domain OrderIds for clientId= $clientId  $it" } }
}
