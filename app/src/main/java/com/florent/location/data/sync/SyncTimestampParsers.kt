package com.florent.location.data.sync

import java.time.Instant

internal fun parseServerEpochMillis(timestamp: String?): Long? =
    timestamp?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() }


internal fun toServerCursorIso(serverUpdatedCursor: Long?): String? {
    val epochMillis = serverUpdatedCursor ?: return null
    val normalizedEpochMillis = if (epochMillis < 100_000_000_000L) epochMillis * 1_000 else epochMillis
    return Instant.ofEpochMilli(normalizedEpochMillis).toString()
}
