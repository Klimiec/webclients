package com.dev.sandbox.restclient.httpclientrestclientapi

import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.stubs.ExternalServiceStubs
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    classes = [HttpclientRestclientApiApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("integration")
class BaseIntegrationTest(val stub: ExternalServiceStubs = ExternalServiceStubs())
