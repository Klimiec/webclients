package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response

import com.fasterxml.jackson.annotation.JsonCreator

data class Order @JsonCreator constructor(
    val orderId: String
)
