package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub.internal

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub.BaseOrderManagementServiceStubBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.Fault
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

    fun willReturnWithFault(clientId: ClientId, fault: Fault) {
        WireMock.stubFor(
            getOrdersFor(clientId).willReturn(WireMock.aResponse().withFault(fault))
        )
    }

    fun withDelay(responseTime: Int) = apply {
        this.responseTime = responseTime
    }
}
