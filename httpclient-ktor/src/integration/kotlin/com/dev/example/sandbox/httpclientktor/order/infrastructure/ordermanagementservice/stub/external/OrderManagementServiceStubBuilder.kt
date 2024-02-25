package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.external

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response.OrdersDto
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.BaseOrderManagementServiceStubBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class OrderManagementServiceStubBuilder : BaseOrderManagementServiceStubBuilder() {
    private var objectMapper: ObjectMapper = ObjectMapper()

    fun willReturnOrdersFor(
        clientId: ClientId,
        response: OrdersDto
    ) {
        WireMock.stubFor(
            getOrdersFor(clientId).willReturn(
                WireMock.aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody(objectMapper.writeValueAsString(response))
            )
        )
    }

    fun verifyGetOrdersCalled(count: Int, clientId: ClientId) {
        WireMock.verify(
            count,
            WireMock.getRequestedFor(WireMock.urlPathEqualTo("/${clientId.clientId}/order"))
        )
    }
}
