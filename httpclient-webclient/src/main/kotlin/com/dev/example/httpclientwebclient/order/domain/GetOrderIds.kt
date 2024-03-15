package com.dev.example.httpclientwebclient.order.domain

interface GetOrderIds {
    suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId>
}
