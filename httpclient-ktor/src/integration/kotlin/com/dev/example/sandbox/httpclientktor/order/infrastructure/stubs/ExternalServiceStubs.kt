package com.dev.example.sandbox.httpclientktor.order.infrastructure.stubs

import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.OrderManagementServiceStubBuilder
import com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes.stub.HermesStubBuilder

class ExternalServiceStubs {
    fun orderManagementService() = OrderManagementServiceStubBuilder()
    fun hermes() = HermesStubBuilder()
}
