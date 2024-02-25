package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import com.github.benmanes.caffeine.cache.Cache
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class CachedGetOrderIdsDecorator(
    private val orderManagementServiceAdapter: OrderManagementServiceAdapter,
    private val cache: Cache<ClientId, List<OrderId>>
) : GetOrderIds {
    override suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId> =
        cache.get(clientId) {
            logger.info { "No cached orderIds for clientId = $clientId. Will call external service to get ones" }
            runBlocking { orderManagementServiceAdapter.getOrderIdsFor(clientId) }
        }

    // for testing
    fun getCache() = cache
}
