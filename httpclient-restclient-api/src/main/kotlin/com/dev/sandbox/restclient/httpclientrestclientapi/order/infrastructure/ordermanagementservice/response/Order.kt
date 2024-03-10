package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.ordermanagementservice.response

data class Order(
    val orderId: String,
    val categoryId: String,
    val countryCode: String,
    val clientId: String,
    val price: Price
) {
    data class Price(
        val amount: String,
        val currency: String
    )
}
