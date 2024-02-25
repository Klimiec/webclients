package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import mu.KotlinLogging
import org.apache.hc.core5.http.NoHttpResponseException
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.net.SocketTimeoutException

private val logger = KotlinLogging.logger {}

fun <T> executeHttpRequest(
    initialLog: String,
    request: RestClient.RequestHeadersSpec<*>,
    successLog: String? = null,
    failureMessage: String,
    bodyType: Class<T>
): T {
    return try {
        logger.info { initialLog }
        request.retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, errorResponse ->
                handle4xxClientError(errorResponse, failureMessage)
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, errorResponse ->
                handle5xxServerError(errorResponse, failureMessage)
            }
            .onStatus(HttpStatusCode::is3xxRedirection) { _, errorResponse ->
                handle3xxClientError(errorResponse, failureMessage)
            }
            .body(bodyType)!!.also {
                if (successLog != null) {
                    logger.info { "$successLog $it" }
                }
            }
    } catch (e: NullPointerException) {
        logger.error(e) { "$failureMessage. Service responded with empty response body." }
        throw ExternalServiceIncorrectResponseBodyException(failureMessage)
    } catch (e: RestClientException) {
        throw handelRestClientException(e, failureMessage)
    }
}

fun executeHttpRequestNoResponse(
    initialLog: String,
    request: RestClient.RequestHeadersSpec<*>,
    successLog: String,
    failureMessage: String
) {
    try {
        logger.info { initialLog }
        request.retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, errorResponse ->
                handle4xxClientError(errorResponse, failureMessage)
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, errorResponse ->
                handle5xxServerError(errorResponse, failureMessage)
            }
            .onStatus(HttpStatusCode::is3xxRedirection) { _, errorResponse ->
                handle3xxClientError(errorResponse, failureMessage)
            }
            .toBodilessEntity().also {
                logger.info { successLog }
            }
    } catch (e: NullPointerException) {
        logger.error(e) { "$failureMessage. Service responded with empty response body." }
        throw ExternalServiceIncorrectResponseBodyException(failureMessage)
    } catch (e: RestClientException) {
        throw handelRestClientException(e, failureMessage)
    }
}

private fun handle4xxClientError(
    response: ClientHttpResponse,
    failureMessage: String
) {
    if (response.statusCode.value() == 404) {
        logger.warn { "$failureMessage. Resource not found(404). Response body= $response" }
        throw ExternalServiceResourceNotFoundException(failureMessage)
    } else if (response.statusCode.value() == 422) {
        logger.error { "$failureMessage. Request validation failed on server side (422 - Unprocessable Entity). Response body= $response" }
        throw ExternalServiceRequestValidationException(failureMessage)
    } else {
        logger.error { "$failureMessage. Service responded with a client error= ${response.statusCode} .Response body= ${response.body}" }
        throw ExternalServiceClientException(failureMessage)
    }
}

private fun handle5xxServerError(response: ClientHttpResponse, failureMessage: String) {
    logger.warn { "$failureMessage .Service responded with a server error= ${response.statusCode.value()} .Response body= $response" }
    throw ExternalServiceServerException(failureMessage)
}

private fun handle3xxClientError(response: ClientHttpResponse, failureMessage: String) {
    logger.error { "$failureMessage. Service responded with a redirection status code= ${response.statusCode.value()}. Response body= $response" }
    throw ExternalServiceRedirectionException(failureMessage)
}

private fun handelRestClientException(e: RestClientException, failureMessage: String): ExternalServiceException {
    if (e.cause is SocketTimeoutException) {
        logger.warn(e) { "$failureMessage. Service failed to deliver response due to read timeout" }
        return ExternalServiceReadTimeoutException(failureMessage)
    } else if (e.cause?.cause is JsonParseException) {
        logger.error { "$failureMessage. Service response cannot be deserialized into response object - possibly missing mandatory field or response has incorrect format" }
        return ExternalServiceIncorrectResponseBodyException(failureMessage)
    } else if (e.cause is NoHttpResponseException) {
        logger.warn(e) { "$failureMessage. Service failed to deliver response." }
        return ExternalServiceNetworkException(failureMessage)
    } else if (e.cause?.cause is MissingKotlinParameterException) {
        logger.error(e) { "$failureMessage. Service response missing mandatory field" }
        return ExternalServiceIncorrectResponseBodyException(failureMessage)
    } else if (e.cause?.cause is com.fasterxml.jackson.databind.exc.MismatchedInputException) {
        logger.error(e) { "$failureMessage. Service response cannot be deserialized into response object - possibly missing mandatory field or response has incorrect format." }
        throw ExternalServiceIncorrectResponseBodyException(failureMessage)
    }
    logger.error(e) { "$failureMessage. Service failed to deliver response" }
    return ExternalServiceResponseException(failureMessage)
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

class ExternalServiceResponseException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)

class ExternalServiceNetworkException(exceptionMessage: String) : ExternalServiceException(exceptionMessage)
