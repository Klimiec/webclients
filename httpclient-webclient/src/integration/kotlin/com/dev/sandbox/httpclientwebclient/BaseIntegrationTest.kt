package com.dev.sandbox.httpclientwebclient

import com.dev.example.httpclientwebclient.HttpclientWebclientApplication
import com.dev.sandbox.httpclientwebclient.order.infrastructure.stubs.ExternalServiceStubs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [HttpclientWebclientApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class BaseIntegrationTest(val stub: ExternalServiceStubs = ExternalServiceStubs())
