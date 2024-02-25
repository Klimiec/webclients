package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.stubs

import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes.stub.HermesStubBuilder
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceStubBuilder

class ExternalServiceStubs {
    fun orderManagementService() = OrderManagementServiceStubBuilder()
    fun hermes() = HermesStubBuilder()
}
