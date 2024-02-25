package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes

import com.dev.sandbox.restclient.httpclientrestclientapi.BaseIntegrationTest
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.InvoiceCreated
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.InvoiceId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.OrderId
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant

@WireMockTest(httpPort = 8082)
class HermesAdapterIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var hermesAdapter: HermesAdapter

    @Test
    fun `should successfully publish InvoiceCreatedEvent`() {
        // given
        stub.hermes().willAcceptInvoiceCreatedEvent()

        // when
        hermesAdapter.publish(sampleInvoiceCreated())

        // then
        stub.hermes().verifyInvoiceCreatedEventPublished()
    }
}

private fun sampleInvoiceCreated(): InvoiceCreated {
    return InvoiceCreated(
        invoiceId = InvoiceId.of("3cc58510-c767-4cc4-8e67-ae1adf201ead"),
        orderId = OrderId.of("63150dfe-2044-472b-a579-f2f2977675d2"),
        timestamp = Instant.now()
    )
}
