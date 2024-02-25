package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response

import com.fasterxml.jackson.annotation.JsonCreator

data class OrdersDto @JsonCreator constructor(val orders: List<Order>)
