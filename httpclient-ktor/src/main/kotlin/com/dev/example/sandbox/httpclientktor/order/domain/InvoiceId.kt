package com.dev.example.sandbox.httpclientktor.order.domain

import java.util.UUID

@JvmInline
value class InvoiceId(val invoiceId: UUID) {
    companion object {
        fun of(invoiceId: String): InvoiceId = InvoiceId(UUID.fromString(invoiceId))
    }
}
