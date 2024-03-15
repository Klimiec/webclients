package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.internal

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class OrderManagementServiceStubBuilder {
    private var responseTime: Int = 0

    fun willReturnOrdersFor(
        clientId: ClientId,
        status: Int = 200,
        response: String?
    ) {
        WireMock.stubFor(
            getOrdersFor(clientId).willReturn(
                WireMock.aResponse()
                    .withFixedDelay(responseTime)
                    .withStatus(status)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(response)
            )
        )
    }

    fun withDelay(responseTime: Int) = apply {
        this.responseTime = responseTime
    }

    private fun getOrdersFor(clientId: ClientId): MappingBuilder =
        WireMock.get("/${clientId.clientId}/order")
//            .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
}
