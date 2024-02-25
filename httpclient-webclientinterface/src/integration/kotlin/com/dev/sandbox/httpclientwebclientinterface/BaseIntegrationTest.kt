package com.dev.sandbox.httpclientwebclientinterface

import com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.stubs.ExternalServiceStubs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [HttpclientWebclientinterfaceApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class BaseIntegrationTest(val stub: ExternalServiceStubs = ExternalServiceStubs())
