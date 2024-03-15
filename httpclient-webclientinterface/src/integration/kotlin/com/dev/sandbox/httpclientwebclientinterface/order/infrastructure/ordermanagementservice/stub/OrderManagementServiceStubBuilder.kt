package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub

import com.dev.sandbox.httpclientwebclientinterface.order.domain.ClientId
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.response.Order
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.Fault
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class OrderManagementServiceStubBuilder {
    private var objectMapper: ObjectMapper = ObjectMapper()
    private var responseTime: Int = 0

    fun willReturnOrdersFor(
        clientId: ClientId,
        response: List<Order>
    ) {
        WireMock.stubFor(
            getOrdersFor(clientId).willReturn(
                WireMock.aResponse()
                    .withFixedDelay(responseTime)
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(objectMapper.writeValueAsString(response))
            )
        )
    }

    fun willReturnOrdersFor(
        clientId: ClientId,
        response: String
    ) = willReturnResponseFor(clientId, 200, response)

    fun willReturnResponseFor(
        clientId: ClientId,
        status: Int,
        body: String?
    ) {
        WireMock.stubFor(
            getOrdersFor(clientId).willReturn(
                WireMock.aResponse()
                    .withStatus(status)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(body)
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

    fun verifyGetOrdersCalled(count: Int, clientId: ClientId) {
        WireMock.verify(
            count,
            WireMock.getRequestedFor(WireMock.urlPathEqualTo("/${clientId.clientId}/order"))
        )
    }

    private fun getOrdersFor(clientId: ClientId): MappingBuilder =
        WireMock.get("/${clientId.clientId}/order")
            .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
}
