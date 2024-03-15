package com.dev.example.sandbox.httpclientktor.order.api

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class TestEndpoint(
    private val orderManagementServiceAdapterRF: GetOrderIds
) {
    @GetMapping("/order")
    suspend fun foo(): List<OrderId> {
        return orderManagementServiceAdapterRF.getOrderIdsFor(ClientId(UUID.randomUUID()))
    }
}
