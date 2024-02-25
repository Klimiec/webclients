package com.dev.example.sandbox.httpclientretrofit

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.stubs.ExternalServiceStubs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [HttpclientRetrofitApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class BaseIntegrationTest(val stub: ExternalServiceStubs = ExternalServiceStubs())
