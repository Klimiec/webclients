package com.dev.sandbox.httpclientwebclientinterface.order.domain

import java.time.Instant

data class InvoiceCreated(
    val invoiceId: InvoiceId,
    val orderId: OrderId,
    val timestamp: Instant
)
