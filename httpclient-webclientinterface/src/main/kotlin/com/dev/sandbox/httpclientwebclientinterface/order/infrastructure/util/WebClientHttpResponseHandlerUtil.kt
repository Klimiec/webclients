package com.dev.sandbox.httpclientwebclientinterface.order.infrastructure.util

import io.netty.handler.timeout.ReadTimeoutException
import mu.KotlinLogging
import org.springframework.core.codec.DecodingException
import org.springframework.web.reactive.function.UnsupportedMediaTypeException
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

private val logger = KotlinLogging.logger {}

suspend fun <T> executeHttpRequest(
    initialLog: String,
    request: suspend () -> T,
    successLog: String? = null,
    failureMessage: String
): T {
    return try {
        logger.info { initialLog }
        val responseBody = request.invoke()?.also {
            if (successLog != null) {
                if (it !is Unit) {
                    logger.info { "$successLog $it" }
                } else {
                    logger.info { successLog }
                }
            }
        }
        responseBody ?: throw NullPointerException("Response body is empty")
    } catch (exception: Exception) {
        throw customException(
            exception = exception,
            failureMessage = failureMessage
        )
    }
}

private fun customException(
    exception: Exception,
    failureMessage: String
): ExternalServiceException {
    when (exception) {
        is WebClientResponseException -> {
            return if (exception.statusCode.is4xxClientError) {
                handle4xxClientError(exception, failureMessage)
            } else if (exception.statusCode.is5xxServerError) {
                handle5xxServerError(exception, failureMessage)
            } else if (exception.statusCode.is3xxRedirection) {
                handle3xxRedirection(exception, failureMessage)
            } else if (exception.cause is UnsupportedMediaTypeException) {
                handleIncorrectResponseBody(exception, failureMessage)
            } else {
                handleUnknownException(exception, failureMessage)
            }
        }

        is WebClientRequestException -> return handleWebClientRequestException(exception, failureMessage)

        is DecodingException -> return handleIncorrectResponseBody(exception, failureMessage)

        is NullPointerException -> return handleIncorrectResponseBody(exception, failureMessage)

        else -> return handleUnknownException(exception, failureMessage)
    }
}

private fun handle3xxRedirection(
    exception: WebClientResponseException,
    failureMessage: String
): ExternalServiceRedirectionException {
    val responseBody = exception.responseBodyAsString
    logger.error {
        "$failureMessage. Service responded with a redirection status code= ${exception.statusCode}. Response body= $responseBody"
    }
    return ExternalServiceRedirectionException(failureMessage)
}

private fun handle5xxServerError(
    exception: WebClientResponseException,
    failureMessage: String
): ExternalServiceServerException {
    val responseBody = exception.responseBodyAsString
    logger.warn { "$failureMessage .Service responded with a server error= ${exception.statusCode} .Response body= $responseBody" }
    return ExternalServiceServerException(failureMessage)
}

private fun handle4xxClientError(
    exception: WebClientResponseException,
    failureMessage: String
): ExternalServiceException {
    val responseBody = exception.responseBodyAsString
    return if (exception.statusCode.value() == 404) {
        logger.warn { "$failureMessage. Resource not found(404). Response body= $responseBody" }
        ExternalServiceResourceNotFoundException(failureMessage)
    } else if (exception.statusCode.value() == 422) {
        logger.error { "$failureMessage. Request validation failed on server side (422 - Unprocessable Entity). Response body= $responseBody" }
        ExternalServiceRequestValidationException(failureMessage)
    } else {
        logger.error { "$failureMessage. Service responded with a client error= ${exception.statusCode} .Response body= $responseBody" }
        ExternalServiceClientException(failureMessage)
    }
}

private fun handleWebClientRequestException(
    clientRequestException: WebClientRequestException,
    failureMessage: String
) = when (clientRequestException.rootCause) {
    is ReadTimeoutException -> {
        logger.warn(clientRequestException) { "$failureMessage. Service failed to deliver response due to read timeout" }
        ExternalServiceReadTimeoutException(failureMessage)
    }

    else -> {
        logger.warn(clientRequestException) { "$failureMessage. Service failed to deliver response." }
        ExternalServiceNetworkException(failureMessage)
    }
}

private fun handleIncorrectResponseBody(
    e: Exception,
    failureMessage: String
): ExternalServiceIncorrectResponseBodyException {
    logger.error(e) { "$failureMessage. Service response cannot be deserialized into response object - possibly missing mandatory field or response has incorrect format" }
    return ExternalServiceIncorrectResponseBodyException(failureMessage)
}

private fun handleUnknownException(
    e: Exception,
    failureMessage: String
): ExternalServiceUnknownException {
    logger.error(e) { "$failureMessage. Service responded with unknown exception" }
    return ExternalServiceUnknownException(failureMessage)
}

sealed class ExternalServiceException(message: String) : Exception(message)

class ExternalServiceResourceNotFoundException(exceptionMessage: String) :
    ExternalServiceException(exceptionMessage)

class ExternalServiceRequestValidationException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceClientException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceServerException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceRedirectionException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceReadTimeoutException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceIncorrectResponseBodyException(exceptionMessage: String) :
    ExternalServiceException(exceptionMessage)

class ExternalServiceUnknownException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceNetworkException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)
