package com.dev.example.sandbox.httpclientktor.order.infrastructure.stubs

import com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes.stub.HermesStubBuilder
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.OrderManagementServiceStubBuilder

class ExternalServiceStubs {
    fun orderManagementService() = OrderManagementServiceStubBuilder()
    fun hermes() = HermesStubBuilder()
}
