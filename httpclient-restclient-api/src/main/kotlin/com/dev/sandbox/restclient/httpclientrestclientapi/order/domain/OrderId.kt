package com.dev.sandbox.restclient.httpclientrestclientapi.order.domain

import java.util.UUID

@JvmInline
value class OrderId(val orderId: UUID) {
    companion object {
        fun of(orderId: String): OrderId = OrderId(UUID.fromString(orderId))
    }
}
