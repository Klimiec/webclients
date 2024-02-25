package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response

import com.fasterxml.jackson.annotation.JsonCreator

data class OrderDto @JsonCreator constructor(
    val orderId: String
)
