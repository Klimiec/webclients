package com.dev.example.sandbox.httpclientktor.order.infrastructure.util

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.JsonConvertException
import mu.KotlinLogging
import org.apache.hc.core5.http.ParseException

const val DEFAULT_SERVER_RESPONSE = "No response body provided (default message)"
private val logger = KotlinLogging.logger {}

suspend fun <T> executeHttpRequest(
    initialLog: String,
    request: suspend () -> T,
    successLog: String? = null,
    failureMessage: String
): T {
    return try {
        logger.info { initialLog }
        val responseBody = request.invoke().also {
            if (successLog != null) {
                if (it is HttpResponse) {
                    logger.info { successLog }
                } else {
                    logger.info { "$successLog $it" }
                }
            }
        }
        responseBody
    } catch (exception: Exception) {
        throw customException(
            exception = exception,
            failureMessage = failureMessage
        )
    }
}

suspend fun customException(
    exception: Exception,
    failureMessage: String
): Throwable {
    when (exception) {
        is ClientRequestException -> {
            val responseBody = exception.response.bodyAsText().ifEmpty { DEFAULT_SERVER_RESPONSE }
            when (exception.response.status.value) {
                404 -> {
                    logger.warn { "$failureMessage. Resource not found(404). Response body= $responseBody" }
                    throw ExternalServiceResourceNotFoundException(failureMessage)
                }

                422 -> {
                    logger.error { "$failureMessage. Request validation failed on server side (422 - Unprocessable Entity). Response body= $responseBody" }
                    throw ExternalServiceRequestValidationException(failureMessage)
                }

                else -> {
                    logger.error { "$failureMessage. Service responded with a client error= ${exception.response.status} .Response body= $responseBody" }
                    throw ExternalServiceClientException(failureMessage)
                }
            }
        }

        is ServerResponseException -> {
            val responseBody = exception.response.bodyAsText().ifEmpty { DEFAULT_SERVER_RESPONSE }
            logger.warn {
                "$failureMessage .Service responded with a server " +
                    "error= ${exception.response.status}.Response body= $responseBody"
            }
            throw ExternalServiceServerException(failureMessage)
        }

        is RedirectResponseException -> {
            val responseBody = exception.response.bodyAsText().ifEmpty { DEFAULT_SERVER_RESPONSE }
            logger.error {
                "$failureMessage. Service responded with a redirection status code= ${exception.response.status}. " +
                    "Response body= $responseBody"
            }
            throw ExternalServiceRedirectionException(failureMessage)
        }

        is HttpRequestTimeoutException -> {
            logger.warn { "$failureMessage. Service failed to deliver response due to read timeout" }
            throw ExternalServiceReadTimeoutException(failureMessage)
        }

        is JsonConvertException -> {
            logger.error { "$failureMessage. Service response cannot be deserialized into response object - possibly missing mandatory field or response has incorrect format" }
            throw ExternalServiceIncorrectResponseBodyException(failureMessage)
        }

        is ParseException -> {
            logger.warn { "$failureMessage. Service failed to deliver response." }
            throw ExternalServiceNetworkException(failureMessage)
        }

        else -> throw ExternalServiceNetworkException(failureMessage)
    }
}

sealed class ExternalServiceException(message: String) : Exception(message)

class ExternalServiceResourceNotFoundException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceRequestValidationException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceClientException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceServerException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceRedirectionException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceReadTimeoutException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceIncorrectResponseBodyException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceNetworkException(failureMessage: String) : ExternalServiceException(failureMessage)
