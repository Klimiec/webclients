package com.dev.example.httpclientwebclient.order.infrastructure.util

import io.micrometer.common.KeyValue
import io.micrometer.common.KeyValues
import org.springframework.web.reactive.function.client.ClientRequestObservationContext
import org.springframework.web.reactive.function.client.DefaultClientRequestObservationConvention

const val SERVICE_NAME = "service.name"

class ExtendedServerRequestObservationConvention : DefaultClientRequestObservationConvention() {

    override fun getLowCardinalityKeyValues(context: ClientRequestObservationContext): KeyValues {
        return super.getLowCardinalityKeyValues(context).and(serviceName(context))
    }

    private fun serviceName(context: ClientRequestObservationContext): KeyValue {
        if (context.request != null) {
            val serviceName = context.request?.attribute(SERVICE_NAME)?.get() as String
            return KeyValue.of(SERVICE_NAME, serviceName)
        }
        return KeyValue.of(SERVICE_NAME, "NONE")
    }
}
