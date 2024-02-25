package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response

import com.fasterxml.jackson.annotation.JsonCreator

data class OrdersDto @JsonCreator constructor(val orders: List<OrderDto>)
