package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response

import com.fasterxml.jackson.annotation.JsonCreator

data class OrdersDto @JsonCreator constructor(val orders: List<Order>)
