package com.dev.example.sandbox.httpclientktor

import com.dev.example.sandbox.httpclientktor.order.infrastructure.stubs.ExternalServiceStubs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [HttpclientKtorApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class BaseIntegrationTest(val stub: ExternalServiceStubs = ExternalServiceStubs())
