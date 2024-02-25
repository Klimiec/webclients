package com.dev.sandbox.httpclientwebclient.order.infrastructure.ordermanagementservice.stub.internal

import com.dev.example.httpclientwebclient.order.domain.ClientId
import com.dev.sandbox.httpclientwebclient.order.infrastructure.ordermanagementservice.stub.BaseOrderManagementServiceStubBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

class OrderManagementServiceStubBuilder : BaseOrderManagementServiceStubBuilder() {
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
}
