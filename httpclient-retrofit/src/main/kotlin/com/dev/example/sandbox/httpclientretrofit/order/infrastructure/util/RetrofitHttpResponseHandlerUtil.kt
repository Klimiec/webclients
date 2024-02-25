package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import com.haroldadmin.cnradapter.NetworkResponse
import mu.KotlinLogging
import java.net.SocketTimeoutException

private const val DEFAULT_SERVER_RESPONSE = "No response body provided (default message)"
private val logger = KotlinLogging.logger {}

suspend fun <T : Any> handleHttpResponse(
    initialLog: String,
    request: suspend () -> NetworkResponse<T, String>,
    successLog: String? = null,
    failureMessage: String
): T {
    logger.info { initialLog }
    return when (val response = request.invoke()) {
        is NetworkResponse.Success -> response.body.also {
            if (successLog != null) {
                if (it !is Unit) {
                    logger.info { "$successLog $it" }
                } else {
                    logger.info { successLog }
                }
            }
        }

        is NetworkResponse.ServerError -> {
            handleErrorResponse(response, failureMessage)
            throw ExternalServiceUnknownException(failureMessage)
        }

        is NetworkResponse.NetworkError -> {
            handleErrorResponse(response, failureMessage)
            throw ExternalServiceNetworkException(failureMessage)
        }

        is NetworkResponse.UnknownError -> {
            logger.error(response.error) { "$failureMessage. Service call failed to deliver response" }
            throw ExternalServiceUnknownException(failureMessage)
        }
    }
}

private fun handleErrorResponse(
    response: NetworkResponse.NetworkError,
    failureMessage: String
) {
    if (response.isReadTimeout()) {
        logger.warn(response.error) { "$failureMessage. Service failed to deliver response due to read timeout" }
        throw ExternalServiceReadTimeoutException(failureMessage)
    } else if (response.isMissingMandatoryField()) {
        logger.error(response.error) { "$failureMessage. Service response missing mandatory field" }
        throw ExternalServiceIncorrectResponseBodyException(failureMessage)
    } else if (response.isNotDeserialized()) {
        logger.error(response.error) { "$failureMessage. Service response cannot be deserialized into response object" }
        throw ExternalServiceIncorrectResponseBodyException(failureMessage)
    }
    logger.warn(response.error) { "$failureMessage. Service call failed to deliver response" }
}

private fun handleErrorResponse(
    response: NetworkResponse.ServerError<String>,
    failureMessage: String
) {
    when (response.code) {
        in 400..499 -> handle4xxClientError(response, failureMessage)
        in 500..599 -> handle5xxServerError(response, failureMessage)
        in 300..399 -> handle3xxRedirection(response, failureMessage)
    }
    logger.error { "$failureMessage .Service response cannot be processed status code= ${response.code}" }
}

private fun handle4xxClientError(
    response: NetworkResponse.ServerError<String>,
    failureMessage: String
) {
    val responseBody = if (response.body.isNullOrBlank()) DEFAULT_SERVER_RESPONSE else response.body!!
    if (response.code == 404) {
        logger.warn { "$failureMessage. Resource not found(404). Response body= $responseBody" }
        throw ExternalServiceResourceNotFoundException(failureMessage)
    } else if (response.code == 422) {
        logger.error { "$failureMessage. Request validation failed on server side (422 - Unprocessable Entity). Response body= $responseBody" }
        throw ExternalServiceRequestValidationException(failureMessage)
    }
    logger.error { "$failureMessage. Service responded with a client error= ${response.code} .Response body= $responseBody" }
    throw ExternalServiceClientException(failureMessage)
}

private fun handle5xxServerError(
    response: NetworkResponse.ServerError<String>,
    failureMessage: String
) {
    val responseBody = if (response.body.isNullOrBlank()) DEFAULT_SERVER_RESPONSE else response.body!!
    logger.warn { "$failureMessage .Service responded with a server error= ${response.code} .Response body= $responseBody" }
    throw ExternalServiceServerException(failureMessage)
}

private fun handle3xxRedirection(
    response: NetworkResponse.ServerError<String>,
    failureMessage: String
) {
    val responseBody = if (response.body.isNullOrBlank()) DEFAULT_SERVER_RESPONSE else response.body!!
    logger.error { "$failureMessage. Service responded with a redirection status code= ${response.code}. Response body= $responseBody" }
    throw ExternalServiceRedirectionException(failureMessage)
}

private fun NetworkResponse.NetworkError.isNotDeserialized(): Boolean {
    return error is MismatchedInputException || error is JsonParseException
}

private fun NetworkResponse.NetworkError.isMissingMandatoryField(): Boolean {
    return error is MissingKotlinParameterException
}

private fun NetworkResponse.NetworkError.isReadTimeout(): Boolean {
    return error is SocketTimeoutException && error.message == "Read timed out"
}

sealed class ExternalServiceException(message: String) : Exception(message)

class ExternalServiceResourceNotFoundException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceRequestValidationException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceClientException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceServerException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceReadTimeoutException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceNetworkException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceRedirectionException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceUnknownException(failureMessage: String) : ExternalServiceException(failureMessage)

class ExternalServiceIncorrectResponseBodyException(exceptionMessage: String) :
    ExternalServiceException(exceptionMessage)
