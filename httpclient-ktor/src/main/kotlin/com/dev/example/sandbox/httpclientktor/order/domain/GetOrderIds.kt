package com.dev.example.sandbox.httpclientktor.order.domain

interface GetOrderIds {
    suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId>
}
