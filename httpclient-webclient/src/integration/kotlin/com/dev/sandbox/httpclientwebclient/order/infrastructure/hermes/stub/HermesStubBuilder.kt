package com.dev.sandbox.httpclientwebclient.order.infrastructure.hermes.stub

import com.dev.example.httpclientwebclient.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

private const val INVOICE_CREATED_URL = "/topics/topic-invoice-created"

class HermesStubBuilder {
    private var responseTime: Int = 0

    fun willAcceptInvoiceCreatedEvent() {
        WireMock.stubFor(
            invoiceCreatedEventTopic()
                .withRequestBody(WireMock.matchingJsonPath("$.invoiceId"))
                .withRequestBody(WireMock.matchingJsonPath("$.orderId"))
                .withRequestBody(WireMock.matchingJsonPath("$.timestamp"))
                .willReturn(
                    WireMock.aResponse()
                        .withFixedDelay(responseTime)
                        .withStatus(HttpStatus.OK.value())
                )
        )
    }

    fun willRejectAcceptInvoiceCreatedEventWith(
        status: Int,
        body: String?
    ): StubMapping = WireMock.stubFor(
        invoiceCreatedEventTopic().willReturn(
            WireMock.aResponse()
                .withStatus(status)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(body)
        )
    )

    fun withDelay(responseTime: Int) = apply {
        this.responseTime = responseTime
    }

    fun verifyInvoiceCreatedEventPublished(event: InvoiceCreatedEventDto) {
        WireMock.verify(
            WireMock.postRequestedFor(WireMock.urlPathEqualTo(INVOICE_CREATED_URL))
                .withRequestBody(WireMock.matchingJsonPath("$.invoiceId", WireMock.equalTo(event.invoiceId)))
                .withRequestBody(WireMock.matchingJsonPath("$.orderId", WireMock.equalTo(event.orderId)))
                .withRequestBody(WireMock.matchingJsonPath("$.timestamp", WireMock.equalTo(event.timestamp)))
        )
    }

    fun verifyInvoiceCreatedEventPublished(count: Int = 1) {
        WireMock.verify(
            count,
            WireMock.postRequestedFor(WireMock.urlPathEqualTo(INVOICE_CREATED_URL))
        )
    }

    private fun invoiceCreatedEventTopic(): MappingBuilder =
        WireMock.post(WireMock.urlPathEqualTo(INVOICE_CREATED_URL))
            .withHeader(HttpHeaders.CONTENT_TYPE, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
}
