package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.GetOrderIds
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.OrderId
import com.github.benmanes.caffeine.cache.Cache
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CachedGetOrderIdsDecorator(
    private val orderManagementServiceAdapter: OrderManagementServiceAdapter,
    private val cache: Cache<ClientId, List<OrderId>>
) : GetOrderIds {
    override fun getOrderIdsFor(clientId: ClientId): List<OrderId> =
        cache.get(clientId) {
            logger.info { "No cached orderIds for clientId = $clientId. Will call external service to get ones" }
            orderManagementServiceAdapter.getOrderIdsFor(clientId)
        }

    // for testing
    fun getCache() = cache
}
