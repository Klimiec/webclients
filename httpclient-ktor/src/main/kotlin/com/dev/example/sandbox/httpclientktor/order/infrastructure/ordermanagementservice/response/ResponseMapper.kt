package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response

import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

fun OrdersDto.toDomain(): List<OrderId> = try {
    this.orders.map { OrderId(UUID.fromString(it.orderId)) }
} catch (e: Exception) {
    logger.error(e) { "Type conversion exception during mapping raw response from Order-Management-Service to domain object" }
    throw OrderManagementServiceResponseMappingException("Type conversion error. ${e.message}")
}

class OrderManagementServiceResponseMappingException(message: String) : Exception(message)
