package com.dev.example.httpclientwebclient.order.infrastructure.hermes

import com.dev.example.httpclientwebclient.order.infrastructure.util.ConnectionProperties
import com.dev.example.httpclientwebclient.order.infrastructure.util.createWebClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(value = [HermesConnectionProperties::class])
class HermesConfiguration {

    @Bean
    fun hermesClient(
        webClientBuilder: WebClient.Builder,
        hermesProperties: HermesConnectionProperties
    ) = HermesClient(
        createWebClient(webClientBuilder, hermesProperties),
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
