package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.ordermanagementservice

import com.dev.example.sandbox.httpclientretrofit.order.domain.ClientId
import com.dev.example.sandbox.httpclientretrofit.order.domain.GetOrderIds
import com.dev.example.sandbox.httpclientretrofit.order.domain.OrderId
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.CacheProperties
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ConnectionProperties
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.createExternalServiceApi
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
@EnableConfigurationProperties(value = [OrderCoreServiceConnectionProperties::class, OrderCoreServiceCacheProperties::class])
class OrderManagementServiceConfiguration {

    @Bean
    fun orderCoreServiceAdapter(
        orderManagementServiceClient: OrderManagementServiceClient,
        orderCoreServiceCacheProperties: OrderCoreServiceCacheProperties,
        meterRegistry: MeterRegistry
    ): GetOrderIds = OrderManagementServiceAdapter(orderManagementServiceClient).let { orderCoreServiceAdapter ->
        if (orderCoreServiceCacheProperties.enabled) {
            val cache = Caffeine.newBuilder()
                .expireAfterWrite(orderCoreServiceCacheProperties.expireAfter)
                .maximumSize(orderCoreServiceCacheProperties.size)
                .recordStats { CaffeineStatsCounter(meterRegistry, orderCoreServiceCacheProperties.name) }
                .build<ClientId, List<OrderId>>()

            return CachedGetOrderIdsDecorator(orderCoreServiceAdapter, cache)
        }
        return orderCoreServiceAdapter
    }

    @Bean
    fun orderCoreServiceClient(
        orderCoreServiceProperties: OrderCoreServiceConnectionProperties,
        objectMapper: ObjectMapper,
        registry: MeterRegistry
    ): OrderManagementServiceClient = OrderManagementServiceClient(
        createExternalServiceApi(orderCoreServiceProperties, objectMapper, registry),
        orderCoreServiceProperties.clientName
    )
}

@ConfigurationProperties(prefix = "services.order-management-service")
data class OrderCoreServiceConnectionProperties(
    override var clientName: String,
    override var baseUrl: String,
    override var connectionTimeout: Long,
    override var readTimeout: Long
) : ConnectionProperties

@ConfigurationProperties(prefix = "services.order-management-service.cache")
data class OrderCoreServiceCacheProperties(
    override var name: String,
    override var enabled: Boolean,
    override var size: Long,
    override var expireAfter: Duration
) : CacheProperties
