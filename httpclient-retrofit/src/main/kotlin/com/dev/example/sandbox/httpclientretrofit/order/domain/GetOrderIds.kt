package com.dev.example.sandbox.httpclientretrofit.order.domain

interface GetOrderIds {
    suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId>
}
