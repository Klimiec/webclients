package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.stubs

import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.hermes.stub.HermesStubBuilder
import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceStubBuilder
import org.springframework.stereotype.Component

@Component
class ExternalServiceStubs {
    fun orderManagementService() = OrderManagementServiceStubBuilder()
    fun hermes() = HermesStubBuilder()
}
