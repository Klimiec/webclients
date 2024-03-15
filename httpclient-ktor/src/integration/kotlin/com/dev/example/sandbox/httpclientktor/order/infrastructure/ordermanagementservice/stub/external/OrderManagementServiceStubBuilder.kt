package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.external

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response.Order
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class OrderManagementServiceStubBuilder {
    private var objectMapper: ObjectMapper = ObjectMapper()

    fun willReturnOrdersFor(
        clientId: ClientId,
        response: List<Order>
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

    private fun getOrdersFor(clientId: ClientId): MappingBuilder =
        WireMock.get("/${clientId.clientId}/order")
//            .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
}
