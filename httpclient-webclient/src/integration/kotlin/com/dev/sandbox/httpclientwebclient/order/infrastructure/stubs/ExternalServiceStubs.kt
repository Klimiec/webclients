package com.dev.sandbox.httpclientwebclient.order.infrastructure.stubs

import com.dev.sandbox.httpclientwebclient.order.infrastructure.hermes.stub.HermesStubBuilder
import com.dev.sandbox.httpclientwebclient.order.infrastructure.ordermanagementservice.stub.OrderManagementServiceStubBuilder

class ExternalServiceStubs {
    fun orderManagementService() = OrderManagementServiceStubBuilder()
    fun hermes() = HermesStubBuilder()
}
