package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice

import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.Order
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface OrderManagementServiceApi {
    @GetExchange(url = "/{clientId}/order", accept = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getOrdersFor(@PathVariable clientId: String): List<Order>
}
