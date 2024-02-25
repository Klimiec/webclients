package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes.request.InvoiceCreatedEventDto
import com.haroldadmin.cnradapter.NetworkResponse
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HermesApi {
    @Headers(
        "Content-Type: application/json",
        "${OkHttpMetricsEventListener.URI_PATTERN}: $URL"
    )
    @POST(URL)
    suspend fun publish(
        @Body body: InvoiceCreatedEventDto
    ): NetworkResponse<Unit, String>

    companion object {
        const val URL = "/topics/topic-invoice-created"
    }
}
