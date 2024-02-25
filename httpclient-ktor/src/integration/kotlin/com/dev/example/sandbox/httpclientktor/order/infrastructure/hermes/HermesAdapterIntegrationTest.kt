package com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientktor.BaseIntegrationTest
import com.dev.example.sandbox.httpclientktor.order.domain.InvoiceCreated
import com.dev.example.sandbox.httpclientktor.order.domain.InvoiceId
import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@WireMockTest(httpPort = 8082)
class HermesAdapterIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var hermesAdapter: HermesAdapter

    @Test
    fun `should successfully publish InvoiceCreatedEvent`(): Unit = runBlocking {
        // given
        stub.hermes().willAcceptInvoiceCreatedEvent()

        // when
        hermesAdapter.publish(sampleInvoiceCreated())

        // then
        stub.hermes().verifyInvoiceCreatedEventPublished()
    }
}

private fun sampleInvoiceCreated() = InvoiceCreated(
    invoiceId = InvoiceId.of("3cc58510-c767-4cc4-8e67-ae1adf201ead"),
    orderId = OrderId.of("63150dfe-2044-472b-a579-f2f2977675d2"),
    timestamp = Instant.now()
)
