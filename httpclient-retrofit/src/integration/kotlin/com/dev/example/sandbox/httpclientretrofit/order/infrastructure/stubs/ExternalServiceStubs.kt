package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.stubs

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.stub.HermesStubBuilder
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceStubBuilder

class ExternalServiceStubs {
    fun orderManagementService() = OrderManagementServiceStubBuilder()
    fun hermes() = HermesStubBuilder()
}
