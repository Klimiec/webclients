package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice.response.Order
import com.haroldadmin.cnradapter.NetworkResponse
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface OrderManagementServiceApi {
    @Headers(
        "Accept: application/json",
        "${OkHttpMetricsEventListener.URI_PATTERN}: $URL"
    )
    @GET(URL)
    suspend fun getOrdersFor(@Path("clientId") clientId: String): NetworkResponse<List<Order>, String>

    companion object {
        const val URL = "/{clientId}/order"
    }
}
