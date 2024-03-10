package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.response

import com.dev.example.sandbox.httpclientretrofit.order.domain.OrderId
import mu.KotlinLogging
import java.util.UUID

private val logger = KotlinLogging.logger {}

fun List<Order>.toDomain(): List<OrderId> = try {
    this.map { OrderId(UUID.fromString(it.orderId)) }
} catch (e: Exception) {
    logger.error(e) { "Type conversion exception during mapping raw response from order-management-service to domain object" }
    throw OrderManagementServiceResponseMappingException("Type conversion error. ${e.message}")
}

class OrderManagementServiceResponseMappingException(message: String) : Exception(message)
