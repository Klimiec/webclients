package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.hermes

import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.ConnectionProperties
import com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util.createExternalServiceApi
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [HermesConnectionProperties::class])
class HermesConfiguration {

    @Bean
    fun hermesAdapter(
        hermesClient: HermesClient
    ) = HermesAdapter(hermesClient)

    @Bean
    fun hermesClient(
        hermesProperties: HermesConnectionProperties,
        objectMapper: ObjectMapper,
        registry: MeterRegistry
    ) = HermesClient(
        createExternalServiceApi(hermesProperties, objectMapper, registry),
        hermesProperties.clientName
    )
}

@ConfigurationProperties(prefix = "services.hermes")
data class HermesConnectionProperties(
    override var clientName: String,
    override var baseUrl: String,
    override var connectionTimeout: Long,
    override var readTimeout: Long
) : ConnectionProperties
