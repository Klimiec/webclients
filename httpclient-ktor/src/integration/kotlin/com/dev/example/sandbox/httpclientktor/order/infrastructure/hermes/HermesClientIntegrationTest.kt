package com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientktor.BaseIntegrationTest
import com.dev.example.sandbox.httpclientktor.order.infrastructure.hermes.stub.HermesFixture
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceClientException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceReadTimeoutException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceRequestValidationException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceResourceNotFoundException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceServerException
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import java.util.stream.Stream

@WireMockTest(httpPort = 8082)
class HermesClientIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var hermesClient: HermesClient

    @Autowired
    lateinit var properties: HermesConnectionProperties

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @Test
    fun `should successfully publish InvoiceCreatedEvent`(): Unit = runBlocking {
        // given
        val invoiceCreatedEvent = HermesFixture.invoiceCreatedEvent()
        stub.hermes().willAcceptInvoiceCreatedEvent()

        // when
        hermesClient.publish(invoiceCreatedEvent)

        // then
        stub.hermes().verifyInvoiceCreatedEventPublished(event = invoiceCreatedEvent)
    }

    @ParameterizedTest(name = "{index}) http status code: {0}")
    @MethodSource("clientErrors")
    fun `when receive response with 4xx status code then throw exception`(
        exceptionClass: Class<Exception>,
        statusCode: Int,
        responseBody: String?
    ): Unit = runBlocking {
        // given
        val invoiceCreatedEvent = HermesFixture.invoiceCreatedEvent()
        stub.hermes().willRejectAcceptInvoiceCreatedEventWith(status = statusCode, body = responseBody)

        // when
        val exception = shouldThrowAny {
            hermesClient.publish(invoiceCreatedEvent)
        }

        // then
        exception.javaClass shouldBeSameInstanceAs exceptionClass
        exception.message shouldContain invoiceCreatedEvent.toString()
        exception.message shouldContain properties.clientName
    }

    @ParameterizedTest(name = "{index}) http status code: {0}")
    @MethodSource("serverErrors")
    fun `when receive response with 5xx status code then throw exception`(
        statusCode: Int,
        responseBody: String?
    ): Unit = runBlocking {
        // given
        val invoiceCreatedEvent = HermesFixture.invoiceCreatedEvent()
        stub.hermes().willRejectAcceptInvoiceCreatedEventWith(status = statusCode, body = responseBody)

        // when
        val exception = shouldThrow<ExternalServiceServerException> {
            hermesClient.publish(invoiceCreatedEvent)
        }
        // then
        exception.message shouldContain invoiceCreatedEvent.toString()
        exception.message shouldContain properties.clientName
    }

    @Test
    fun `when service returns above timeout threshold then throw exception`(): Unit = runBlocking {
        // given
        val invoiceCreatedEvent = HermesFixture.invoiceCreatedEvent()

        stub.hermes()
            .withDelay(properties.readTimeout.toInt() + 100)
            .willAcceptInvoiceCreatedEvent()

        // when
        val exception = shouldThrow<ExternalServiceReadTimeoutException> {
            hermesClient.publish(invoiceCreatedEvent)
        }
        // then
        exception.message shouldContain invoiceCreatedEvent.toString()
        exception.message shouldContain properties.clientName
    }

    @Test
    fun `verify custom tags for hermes (metrics)`(): Unit = runBlocking {
        // given
        meterRegistry.clear()
        val invoiceCreatedEvent = HermesFixture.invoiceCreatedEvent()
        stub.hermes().willAcceptInvoiceCreatedEvent()

        // when
        hermesClient.publish(invoiceCreatedEvent)

        // then
        /*      TODO: find how to enable metrics for ktor
                meterRegistry.get("http.client.requests").tags("service.name", properties.clientName).timer()
                    .count() shouldBeExactly 1*/
    }

    companion object {
        @JvmStatic
        fun clientErrors(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(ExternalServiceClientException::class.java, HttpStatus.BAD_REQUEST.value(), null),
                Arguments.of(
                    ExternalServiceResourceNotFoundException::class.java,
                    HttpStatus.NOT_FOUND.value(),
                    """{"message": "Path does not exist"}"""
                ),
                Arguments.of(
                    ExternalServiceRequestValidationException::class.java,
                    HttpStatus.UNPROCESSABLE_ENTITY.value(),
                    """{"message": "Validation Failed"}"""
                )
            )
        }

        @JvmStatic
        fun serverErrors(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    """{"message": "Server encountered an internal error"}"""
                ),
                Arguments.of(HttpStatus.SERVICE_UNAVAILABLE.value(), null)
            )
        }
    }
}
