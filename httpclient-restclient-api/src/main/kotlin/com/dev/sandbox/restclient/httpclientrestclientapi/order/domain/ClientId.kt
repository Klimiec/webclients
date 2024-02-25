package com.dev.sandbox.restclient.httpclientrestclientapi.order.domain

import java.util.UUID

@JvmInline
value class ClientId(val clientId: UUID) {
    override fun toString(): String {
        return clientId.toString()
    }
}
