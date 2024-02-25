package com.dev.sandbox.httpclientwebclientinterface.order.domain

interface GetOrderIds {
    suspend fun getOrderIdsFor(clientId: ClientId): List<OrderId>
}
