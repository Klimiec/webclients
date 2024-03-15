package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice

import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.ClientId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.GetOrderIds
import com.dev.sandbox.restclient.httpclientrestclientapi.order.domain.OrderId
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.CacheProperties
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.ConnectionProperties
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.ExtendedServerRequestObservationConvention
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.createRestClient
import com.github.benmanes.caffeine.cache.Caffeine
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.cache.CaffeineStatsCounter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
@EnableConfigurationProperties(value = [OrderManagementServiceConnectionProperties::class, OrderManagementServiceCacheProperties::class])
class OrderManagementServiceConfiguration {

    @Bean
    fun orderManagementServiceClient(
        restClientBuilder: RestClient.Builder,
        orderManagementServiceProperties: OrderManagementServiceConnectionProperties
    ) = OrderManagementServiceClient(
        createRestClient(restClientBuilder, orderManagementServiceProperties),
        orderManagementServiceProperties.clientName
    )

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
                .softValues()
                .build<ClientId, List<OrderId>>()

            return CachedGetOrderIdsDecorator(orderManagementServiceAdapter, cache)
        }
        return orderManagementServiceAdapter
    }

    @Bean
    fun extendedServerRequestObservationConvention() = ExtendedServerRequestObservationConvention()
}

@ConfigurationProperties(prefix = "services.order-management-service")
data class OrderManagementServiceConnectionProperties(
    override var clientName: String,
    override var baseUrl: String,
    override var connectionTimeout: Int,
    override var readTimeout: Long
) : ConnectionProperties

@ConfigurationProperties(prefix = "services.order-management-service.cache")
data class OrderManagementServiceCacheProperties(
    override var name: String,
    override var enabled: Boolean,
    override var size: Long,
    override var expireAfter: Duration
) : CacheProperties
