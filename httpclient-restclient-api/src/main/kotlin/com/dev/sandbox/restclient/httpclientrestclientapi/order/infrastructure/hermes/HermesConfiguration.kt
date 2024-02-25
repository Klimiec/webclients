package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.hermes

import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.ConnectionProperties
import com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util.createRestClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
@EnableConfigurationProperties(value = [HermesConnectionProperties::class])
class HermesConfiguration {

    @Bean
    fun hermesClient(
        restClientBuilder: RestClient.Builder,
        hermesProperties: HermesConnectionProperties
    ) = HermesClient(
        createRestClient(restClientBuilder, hermesProperties),
        hermesProperties.clientName
    )

    @Bean
    fun hermesAdapter(
        hermesClient: HermesClient
    ) = HermesAdapter(hermesClient)
}

@ConfigurationProperties(prefix = "services.hermes")
data class HermesConnectionProperties(
    override var clientName: String,
    override var baseUrl: String,
    override var connectionTimeout: Int,
    override var readTimeout: Long
) : ConnectionProperties
