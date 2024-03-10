package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.haroldadmin.cnradapter.NetworkResponse
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface HermesApi {
    @Headers(
        "Content-Type: application/json",
        "${OkHttpMetricsEventListener.URI_PATTERN}: $URL"
    )
    @POST(URL)
    suspend fun publish(
        @Body body: InvoiceCreatedEventDto,
        @Path("topic") topic: String = TOPIC_INVOICE_CREATED_EVENT,
    ): NetworkResponse<Unit, String>

    companion object {
        const val URL = "/topics/{topic}"
        const val TOPIC_INVOICE_CREATED_EVENT = "topic-invoice-created"
    }
}