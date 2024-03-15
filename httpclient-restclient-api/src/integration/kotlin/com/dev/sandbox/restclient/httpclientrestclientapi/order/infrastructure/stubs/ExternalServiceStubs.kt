package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.stubs

import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceStubBuilder
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes.stub.HermesStubBuilder

class ExternalServiceStubs {
    fun orderManagementService() = OrderManagementServiceStubBuilder()
    fun hermes() = HermesStubBuilder()
}
