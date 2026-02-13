package com.florent.location.data.sync

import java.time.Instant

internal fun parseServerEpochMillis(timestamp: String?): Long? =
    timestamp?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }


internal fun toServerCursorIso(serverUpdatedCursor: Long?): String? {
    val epochMillis = serverUpdatedCursor ?: return null
    return Instant.ofEpochMilli(epochMillis).toString()
}
