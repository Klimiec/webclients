package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientretrofit.BaseIntegrationTest
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.stub.OrderCoreServiceFixture.anyClientId
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.stub.OrderCoreServiceFixture.ordersPlacedByPolishCustomer
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceClientException
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceIncorrectResponseBodyException
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceNetworkException
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceReadTimeoutException
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceRedirectionException
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceRequestValidationException
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceResourceNotFoundException
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ExternalServiceServerException
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.matchers.longs.shouldBeExactly
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
internal class OrderManagementServiceClientIntegrationTest : BaseIntegrationTest() {
    @Autowired
    lateinit var orderManagementServiceClient: OrderManagementServiceClient

    @Autowired
    lateinit var properties: OrderCoreServiceConnectionProperties

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @Test
    fun `should return orders for a given clientId`(): Unit = runBlocking {
        // given
        val clientId = anyClientId()
        stubs.orderManagementService().willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer())

        // when
        val response = orderManagementServiceClient.getOrdersFor(clientId)

        // then
        response.size shouldBe 1
        response[0].orderId shouldBe "7952a9ab-503c-4483-beca-32d081cc2446"
        response[0].categoryId shouldBe "327456"
        response[0].countryCode shouldBe "PL"
        response[0].clientId shouldBe "1a575762-0903-4b7a-9da3-d132f487c5ae"
        response[0].price.amount shouldBe "1500"
        response[0].price.currency shouldBe "PLN"
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
        stubs.orderManagementService().willReturnResponseFor(clientId, status = statusCode, response = responseBody)

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
        stubs.orderManagementService().willReturnResponseFor(clientId, status = statusCode, response = responseBody)

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

        stubs.orderManagementService()
            .withDelay(properties.readTimeout.toInt() + 500)
            .willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer(clientId = clientId.toString()))

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
        stubs.orderManagementService().willReturnOrdersFor(clientId, response = responseBody)

        // when
        val exception = shouldThrow<ExternalServiceIncorrectResponseBodyException> {
            orderManagementServiceClient.getOrdersFor(clientId)
        }
        // then
        exception.message shouldContain clientId.clientId.toString()
        exception.message shouldContain properties.clientName
    }

    @Test
    fun `when service closes connection then throw exception`(): Unit = runBlocking {
        // given: "close connection before delivering full response"
        val clientId = anyClientId()
        stubs.orderManagementService().willReturnWithFault(clientId, fault = Fault.RANDOM_DATA_THEN_CLOSE)

        // when
        val exception = shouldThrow<ExternalServiceNetworkException> {
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
        stubs.orderManagementService().willReturnResponseFor(clientId, status = statusCode, response = responseBody)

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
        stubs.orderManagementService()
            .willReturnOrdersFor(clientId, response = ordersPlacedByPolishCustomer(clientId = clientId.toString()))

        // when
        orderManagementServiceClient.getOrdersFor(clientId)

        // then
        meterRegistry.get("http.client.requests").tags("service.name", properties.clientName).timer()
            .count() shouldBeExactly 1
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
                // service returns orderid instead of order_id
                Arguments.of("""[{"orderid": "34827304-293b-11ee-be56-0242ac120002"}]"""),
                // empty response
                Arguments.of(""),
                // incorrect response payload
                Arguments.of("Lorem Ipsum")
            )
        }
    }
}
