package com.dev.example.httpclientwebclient.order.infrastructure.ordermanagementservice.response

import com.fasterxml.jackson.annotation.JsonCreator

data class Order @JsonCreator constructor(
    val orderId: String
)
