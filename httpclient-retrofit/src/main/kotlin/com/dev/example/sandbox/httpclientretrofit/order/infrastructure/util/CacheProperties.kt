package com.dev.example.sandbox.httpclientretrofit.order.infrastructure.util

import java.time.Duration

interface CacheProperties {
    var name: String
    var enabled: Boolean
    var size: Long
    var expireAfter: Duration
}
