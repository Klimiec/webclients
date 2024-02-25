package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.stub

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

open class BaseOrderManagementServiceStubBuilder {

    fun getOrdersFor(clientId: ClientId): MappingBuilder =
        WireMock.get("/${clientId.clientId}/order")
            .withHeader(HttpHeaders.ACCEPT, WireMock.equalTo(MediaType.APPLICATION_JSON_VALUE))
}
