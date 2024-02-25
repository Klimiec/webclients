package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.OrdersDto
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface OrderManagementServiceApi {
    @GetExchange(url = "/{clientId}/order", accept = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getOrdersFor(@PathVariable clientId: ClientId): OrdersDto
}
