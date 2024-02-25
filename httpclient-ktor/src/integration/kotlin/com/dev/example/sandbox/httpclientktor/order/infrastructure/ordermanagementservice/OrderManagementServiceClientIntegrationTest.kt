package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.BaseIntegrationTest
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response.Order
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.response.OrdersDto
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.external.OrderManagementServiceFixture.anyClientId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.internal.OrderManagementServiceContractFixture.ordersPlacedBySomeCustomer
import com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice.stub.internal.OrderManagementServiceStubBuilder
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceClientException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceIncorrectResponseBodyException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceReadTimeoutException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceRedirectionException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceRequestValidationException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceResourceNotFoundException
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ExternalServiceServerException
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.shouldBe
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
class OrderManagementServiceClientIntegrationTest : BaseIntegrationTest() {
    @Autowired
    lateinit var orderManagementServiceClient: OrderManagementServiceClient

    @Autowired
    lateinit var properties: OrderManagementServiceConnectionProperties

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    private var orderManagementServiceStub = OrderManagementServiceStubBuilder()

    @Test
    fun `should return orders for a given clientId`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        orderManagementServiceStub.willReturnOrdersFor(clientId, response = ordersPlacedBySomeCustomer())

        // when
        val response = orderManagementServiceClient.getOrdersFor(clientId)

        // then
        response shouldBe OrdersDto(listOf(Order("7952a9ab-503c-4483-beca-32d081cc2446")))
    }

    @ParameterizedTest(name = "{index}) http status code: {0}")
    @MethodSource("clientErrors")
    fun `when receive response with 4xx status code then throw exception`(
        exceptionClass: Class<Exception>,
        statusCode: Int,
        responseBody: String?
    ): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        orderManagementServiceStub.willReturnOrdersFor(clientId, status = statusCode, response = responseBody)

        // when
        val exception = shouldThrowAny {
            orderManagementServiceClient.getOrdersFor(clientId)
        }

        // then
        exception.javaClass shouldBeSameInstanceAs exceptionClass
        exception.message shouldContain clientId.clientId.toString()
        exception.message shouldContain properties.clientName
    }

    @ParameterizedTest(name = "{index}) http status code: {0}")
    @MethodSource("serverErrors")
    fun `when receive response with 5xx status code then throw exception`(
        statusCode: Int,
        responseBody: String?
    ): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        orderManagementServiceStub.willReturnOrdersFor(clientId, status = statusCode, response = responseBody)

        // when
        val exception = shouldThrow<ExternalServiceServerException> {
            orderManagementServiceClient.getOrdersFor(clientId)
        }
        // then
        exception.message shouldContain clientId.clientId.toString()
        exception.message shouldContain properties.clientName
    }

    @Test
    fun `when service returns above timeout threshold then throw exception`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()

        orderManagementServiceStub
            .withDelay(properties.readTimeout.toInt())
            .willReturnOrdersFor(clientId, response = ordersPlacedBySomeCustomer())

        // when
        val exception = shouldThrow<ExternalServiceReadTimeoutException> {
            orderManagementServiceClient.getOrdersFor(clientId)
        }
        // then
        exception.message shouldContain clientId.clientId.toString()
        exception.message shouldContain properties.clientName
    }

    @ParameterizedTest(name = "{index}) response body: {0}")
    @MethodSource("incorrectResponseBody")
    fun `when receive response with incorrect body then throw exception`(responseBody: String): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        orderManagementServiceStub.willReturnOrdersFor(clientId, response = responseBody)

        // when
        val exception = shouldThrow<ExternalServiceIncorrectResponseBodyException> {
            orderManagementServiceClient.getOrdersFor(clientId)
        }
        // then
        exception.message shouldContain clientId.clientId.toString()
        exception.message shouldContain properties.clientName
    }

    @ParameterizedTest(name = "{index}) http status code: {0}")
    @MethodSource("redirectErrors")
    fun `when receive response with 3xx status code then throw exception`(
        statusCode: Int,
        responseBody: String?
    ): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        orderManagementServiceStub.willReturnOrdersFor(clientId, status = statusCode, response = responseBody)

        // when
        val exception = shouldThrow<ExternalServiceRedirectionException> {
            orderManagementServiceClient.getOrdersFor(clientId)
        }
        // then
        exception.message shouldContain clientId.clientId.toString()
        exception.message shouldContain properties.clientName
    }

    @Test
    fun `verify custom tags for order-management-service (metrics)`(): Unit = runBlocking {
        // given
        meterRegistry.clear()
        // and
        val clientId = anyClientId()
        orderManagementServiceStub
            .willReturnOrdersFor(clientId, response = ordersPlacedBySomeCustomer())

        // when
        orderManagementServiceClient.getOrdersFor(clientId)

        // then
        /*  TODO: find how to enable metrics for ktor
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

        @JvmStatic
        fun redirectErrors(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(HttpStatus.MOVED_PERMANENTLY.value(), """{"message": "MOVED_PERMANENTLY"}"""),
                Arguments.of(HttpStatus.FOUND.value(), null)
            )
        }

        @JvmStatic
        fun incorrectResponseBody(): Stream<Arguments> {
            return Stream.of(
                // service returns order with missing mandatory fields
                Arguments.of("""{"orders": ["categoryIds": "327456"]}"""),
                // empty response
                Arguments.of(""),
                // incorrect response payload
                Arguments.of("Lorem Ipsum")
            )
        }
    }
}
