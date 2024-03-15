package com.dev.sandbox.restclient.httpclientrestclientapi.order.infrastructure.util

import io.micrometer.common.KeyValue
import io.micrometer.common.KeyValues
import org.springframework.http.client.observation.ClientRequestObservationContext
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention

const val SERVICE_NAME = "service.name"

class ExtendedServerRequestObservationConvention : DefaultClientRequestObservationConvention() {

    override fun getLowCardinalityKeyValues(context: ClientRequestObservationContext): KeyValues {
        return super.getLowCardinalityKeyValues(context).and(serviceName(context))
    }

    private fun serviceName(context: ClientRequestObservationContext): KeyValue {
        if (context.carrier != null) {
            return KeyValue.of(SERVICE_NAME, context.carrier!!.headers[SERVICE_NAME]?.first() ?: "NONE")
        }
        return KeyValue.of(SERVICE_NAME, "NONE")
    }
}
