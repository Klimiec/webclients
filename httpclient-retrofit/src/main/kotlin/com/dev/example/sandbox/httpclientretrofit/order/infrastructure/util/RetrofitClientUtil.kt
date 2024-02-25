package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.haroldadmin.cnradapter.NetworkResponseAdapterFactory
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

inline fun <reified T> createExternalServiceApi(
    properties: ConnectionProperties,
    objectMapper: ObjectMapper,
    registry: MeterRegistry
): T {
    return Retrofit.Builder()
        .client(okHttpClient(properties, registry))
        .baseUrl(properties.baseUrl)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .addCallAdapterFactory(NetworkResponseAdapterFactory())
        .build()
        .create(T::class.java)
}

fun okHttpClient(properties: ConnectionProperties, registry: MeterRegistry): OkHttpClient {
    return OkHttpClient()
        .newBuilder()
        .connectTimeout(properties.connectionTimeout, TimeUnit.MILLISECONDS)
        .readTimeout(properties.readTimeout, TimeUnit.MILLISECONDS)
//        .writeTimeout(properties.readTimeout, TimeUnit.MILLISECONDS)
        .writeTimeout(3000, TimeUnit.MILLISECONDS)
        .followRedirects(false)
        .addInterceptor(httpLoggingInterceptor())
        .eventListener(
            OkHttpMetricsEventListener.builder(registry, "http.client.requests")
                .tags(Tags.of("service.name", properties.clientName))
                .build()
        )
        .connectionPool(ConnectionPool(10, 50_000, TimeUnit.MILLISECONDS))
        .dispatcher(customDispatcher())
        .build()
}

private fun httpLoggingInterceptor(): HttpLoggingInterceptor {
    val logging = HttpLoggingInterceptor()
    logging.setLevel(HttpLoggingInterceptor.Level.BASIC)
    logging.redactHeader("Authorization")
    logging.redactHeader("Cookie")
    return logging
}

private fun customDispatcher(): Dispatcher {
    val dispatcher = Dispatcher()
    dispatcher.maxRequests = 100
    dispatcher.maxRequestsPerHost = 100
    return dispatcher
}

interface ConnectionProperties {
    var clientName: String
    var baseUrl: String
    var connectionTimeout: Long
    var readTimeout: Long
}
