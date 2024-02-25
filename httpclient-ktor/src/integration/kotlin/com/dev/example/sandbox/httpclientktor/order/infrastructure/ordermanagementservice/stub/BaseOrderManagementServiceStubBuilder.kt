package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock

open class BaseOrderManagementServiceStubBuilder {

    fun getOrdersFor(clientId: ClientId): MappingBuilder =
        WireMock.get("/${clientId.clientId}/order")
//            .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
}
