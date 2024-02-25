package com.dev.example.sandbox.httpclientktor.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientktor.order.domain.ClientId
import com.dev.example.sandbox.httpclientktor.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientktor.order.domain.OrderId
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.CacheProperties
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.ConnectionProperties
import com.dev.example.sandbox.httpclientktor.order.infrastructure.util.createHttpClient
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.cache.CaffeineStatsCounter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableConfigurationProperties(value = [OrderManagementServiceConnectionProperties::class, OrderManagementServiceCacheProperties::class])
class OrderManagementServiceConfiguration {

    @Bean
    fun orderManagementServiceAdapter(
        orderManagementServiceClient: OrderManagementServiceClient,
        orderManagementServiceCacheProperties: OrderManagementServiceCacheProperties,
        meterRegistry: MeterRegistry
    ): GetOrderIds = OrderManagementServiceAdapter(orderManagementServiceClient).let { orderManagementServiceAdapter ->
        if (orderManagementServiceCacheProperties.enabled) {
            val cache = Caffeine.newBuilder()
                .expireAfterWrite(orderManagementServiceCacheProperties.expireAfter)
                .maximumSize(orderManagementServiceCacheProperties.size)
                .recordStats { CaffeineStatsCounter(meterRegistry, orderManagementServiceCacheProperties.name) }
                .build<ClientId, List<OrderId>>()

            return CachedGetOrderIdsDecorator(orderManagementServiceAdapter, cache)
        }
        return orderManagementServiceAdapter
    }

    @Bean
    fun orderManagementServiceClient(
        orderManagementServiceProperties: OrderManagementServiceConnectionProperties,
        objectMapper: ObjectMapper
    ) = OrderManagementServiceClient(
        createHttpClient(orderManagementServiceProperties),
        orderManagementServiceProperties.clientName
    )
}

@ConfigurationProperties(prefix = "services.order-management-service")
data class OrderManagementServiceConnectionProperties(
    override var clientName: String,
    override var baseUrl: String,
    override var connectionTimeout: Long,
    override var readTimeout: Long
) : ConnectionProperties

@ConfigurationProperties(prefix = "services.order-management-service.cache")
data class OrderManagementServiceCacheProperties(
    override var name: String,
    override var enabled: Boolean,
    override var size: Long,
    override var expireAfter: Duration
) : CacheProperties
