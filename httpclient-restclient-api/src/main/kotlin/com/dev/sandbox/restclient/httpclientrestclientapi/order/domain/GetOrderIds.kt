package com.dev.sandbox.restclient.httpclientrestclientapi.order.domain

interface GetOrderIds {
    fun getOrderIdsFor(clientId: ClientId): List<OrderId>
}
