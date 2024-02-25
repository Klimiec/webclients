package com.dev.example.httpclientwebclient.order.infrastructure.util

import io.netty.handler.timeout.ReadTimeoutException
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import mu.KotlinLogging
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatusCode
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException

private const val DEFAULT_SERVER_RESPONSE = "No response body provided (default message)"
val webLogger = KotlinLogging.logger {}

suspend inline fun <reified T> executeHttpRequest(
    initialLog: String,
    request: WebClient.RequestHeadersSpec<*>,
    successLog: String? = null,
    failureMessage: String
): T {
    try {
        webLogger.info { initialLog }
        return request
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                handle4xxClientError(it, failureMessage)
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                handle5xxServerError(it, failureMessage)
            }
            .onStatus(HttpStatusCode::is3xxRedirection) {
                handle3xxRedirection(it, failureMessage)
            }
            .bodyToMono(T::class.java)
            .onErrorMap(WebClientRequestException::class.java) { handleWebClientRequestException(it, failureMessage) }
            .onErrorMap(WebClientResponseException::class.java) { handleWebClientResponseException(it, failureMessage) }
            .onErrorMap(DecodingException::class.java) { handleDecodingException(it, failureMessage) }
            .awaitSingle().also {
                if (successLog != null) {
                    webLogger.info { "$successLog $it" }
                }
            }
    } catch (e: NoSuchElementException) {
        webLogger.error(e) { "$failureMessage. Service responded with empty response body." }
        throw ExternalServiceIncorrectResponseBodyException(failureMessage)
    }
}

suspend fun executeHttpRequestNoResponse(
    initialLog: String,
    request: WebClient.RequestHeadersSpec<*>,
    successLog: String? = null,
    failureMessage: String
) = try {
    webLogger.info { initialLog }
    request
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError) {
            handle4xxClientError(it, failureMessage)
        }
        .onStatus(HttpStatusCode::is5xxServerError) {
            handle5xxServerError(it, failureMessage)
        }
        .onStatus(HttpStatusCode::is3xxRedirection) {
            handle3xxRedirection(it, failureMessage)
        }
        .toBodilessEntity()
        .onErrorMap(WebClientRequestException::class.java) { handleWebClientRequestException(it, failureMessage) }
        .onErrorMap(WebClientResponseException::class.java) { handleWebClientResponseException(it, failureMessage) }
        .onErrorMap(DecodingException::class.java) { handleDecodingException(it, failureMessage) }
        .awaitSingleOrNull().also {
            if (successLog != null) {
                webLogger.info { "$successLog $it" }
            }
        }
} catch (e: NoSuchElementException) {
    webLogger.error(e) { "$failureMessage. Service responded with empty response body." }
    throw ExternalServiceIncorrectResponseBodyException(failureMessage)
}

fun handle4xxClientError(
    response: ClientResponse,
    failureMessage: String
) = response.bodyToMono(String::class.java)
    .defaultIfEmpty(DEFAULT_SERVER_RESPONSE)
    .map { responseBody ->
        if (response.statusCode().value() == 404) {
            webLogger.warn { "$failureMessage. Resource not found(404). Response body= $responseBody" }
            ExternalServiceResourceNotFoundException(failureMessage)
        } else if (response.statusCode().value() == 422) {
            webLogger.error { "$failureMessage. Request validation failed on server side (422 - Unprocessable Entity). Response body= $responseBody" }
            ExternalServiceRequestValidationException(failureMessage)
        } else {
            webLogger.error { "$failureMessage. Service responded with a client error= ${response.statusCode()} .Response body= $responseBody" }
            ExternalServiceClientException(failureMessage)
        }
    }

fun handle5xxServerError(
    response: ClientResponse,
    failureMessage: String
) = response.bodyToMono(String::class.java)
    .defaultIfEmpty(DEFAULT_SERVER_RESPONSE)
    .map { responseBody ->
        webLogger.warn {
            "$failureMessage .Service responded with a server error= ${response.statusCode().value()}" +
                ".Response body= $responseBody"
        }
        ExternalServiceServerException(failureMessage)
    }

fun handle3xxRedirection(
    response: ClientResponse,
    failureMessage: String
) = response.bodyToMono(String::class.java)
    .defaultIfEmpty(DEFAULT_SERVER_RESPONSE)
    .map { responseBody ->
        webLogger.error {
            "$failureMessage. Service responded with a redirection status code= " +
                "${response.statusCode().value()}. Response body= $responseBody"
        }
        ExternalServiceRedirectionException(failureMessage)
    }

fun handleWebClientRequestException(
    clientRequestException: WebClientRequestException,
    failureMessage: String
) = when (clientRequestException.rootCause) {
    is ReadTimeoutException -> {
        webLogger.warn(clientRequestException) { "$failureMessage. Service failed to deliver response due to read timeout. ${clientRequestException.message}" }
        ExternalServiceReadTimeoutException(failureMessage)
    }

    else -> {
        webLogger.warn(clientRequestException) { "$failureMessage. Service failed to deliver response." }
        ExternalServiceNetworkException(failureMessage)
    }
}

fun handleWebClientResponseException(
    clientResponseException: WebClientResponseException,
    failureMessage: String
): Throwable {
    webLogger.warn(clientResponseException) { "$failureMessage. Service response cannot be processed status code= ${clientResponseException.statusCode}" }
    throw ExternalServiceResponseException(failureMessage)
}

fun handleDecodingException(
    e: DecodingException,
    failureMessage: String
): ExternalServiceIncorrectResponseBodyException {
    webLogger.error(e) { "$failureMessage. Service response cannot be deserialized into response object - possibly missing mandatory field or response has incorrect format" }
    throw ExternalServiceIncorrectResponseBodyException(failureMessage)
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
