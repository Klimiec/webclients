package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.GetOrderIds
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.OrderId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response.toDomain
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class OrderManagementServiceAdapter(private val orderManagementServiceClient: OrderManagementServiceClient) : GetOrderIds {
    override fun getOrderIdsFor(clientId: ClientId): List<OrderId> =
        orderManagementServiceClient.getOrdersFor(clientId)
            .toDomain()
            .also { logger.info { "OrderIds for clientId= $clientId  $it" } }
}
