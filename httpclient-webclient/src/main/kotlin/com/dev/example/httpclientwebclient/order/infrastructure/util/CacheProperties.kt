package com.dev.example.httpclientwebclient.order.infrastructure.util

import java.time.Duration

interface CacheProperties {
    var name: String
    var enabled: Boolean
    var size: Long
    var expireAfter: Duration
}
