package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.request

import com.dev.example.sandbox.httpclientretrofit.order.domain.InvoiceCreated
import com.dev.example.sandbox.httpclientretrofit.order.domain.InvoiceId
import com.dev.example.sandbox.httpclientretrofit.order.domain.OrderId
import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Test
import java.time.Instant

class RequestMapperKtTest {

    @Test
    fun `should map domain event to dto`() {
        // given
        val invoiceCreated = sampleInvoiceCreated()

        // when
        val result = invoiceCreated.mapToPublishInvoiceCreatedEventDto()

        // then
        result shouldContainsDataFrom invoiceCreated
    }
}

private fun sampleInvoiceCreated(): InvoiceCreated {
    return InvoiceCreated(
        invoiceId = InvoiceId.of("3cc58510-c767-4cc4-8e67-ae1adf201ead"),
        orderId = OrderId.of("63150dfe-2044-472b-a579-f2f2977675d2"),
        timestamp = Instant.now()
    )
}
private infix fun InvoiceCreatedEventDto.shouldContainsDataFrom(invoiceCreated: InvoiceCreated): InvoiceCreatedEventDto {
    this.invoiceId shouldBeEqual invoiceCreated.invoiceId.invoiceId.toString()
    this.orderId shouldBeEqual invoiceCreated.orderId.orderId.toString()
    this.timestamp shouldBeEqual invoiceCreated.timestamp.toString()
    return this
}
