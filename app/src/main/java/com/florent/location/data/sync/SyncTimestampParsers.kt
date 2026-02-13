package com.florent.location.data.sync

import java.time.Instant

internal fun parseServerEpochMillis(timestamp: String?): Long? =
    timestamp?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }

internal fun parseServerEpochSeconds(timestamp: String?): Long? =
    timestamp?.let { runCatching { Instant.parse(it).epochSecond }.getOrNull() }
