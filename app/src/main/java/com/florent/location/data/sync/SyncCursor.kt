package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.entity.SyncCursorEntity

internal data class CompositeSyncCursor(
    val updatedAtEpochMillis: Long,
    val remoteId: String
)

internal fun SyncCursorEntity.toCompositeCursor(): CompositeSyncCursor =
    CompositeSyncCursor(updatedAtEpochMillis = updatedAtEpochMillis, remoteId = remoteId)

internal fun CompositeSyncCursor.toEntity(userId: String, syncKey: String): SyncCursorEntity =
    SyncCursorEntity(userId = userId, syncKey = syncKey, updatedAtEpochMillis = updatedAtEpochMillis, remoteId = remoteId)

internal fun hasInvalidUpdatedAt(updatedAtIso: String?): Boolean = parseServerEpochMillis(updatedAtIso) == null

internal fun isAfterCursor(updatedAtIso: String?, remoteId: String, cursor: CompositeSyncCursor?): Boolean {
    if (cursor == null) return true
    val updatedAtMillis = parseServerEpochMillis(updatedAtIso)
    if (updatedAtMillis == null) {
        Log.w("SyncCursor", "Invalid updated_at for remote_id=$remoteId, applying row conservatively")
        return true
    }
    return updatedAtMillis > cursor.updatedAtEpochMillis ||
        (updatedAtMillis == cursor.updatedAtEpochMillis && remoteId > cursor.remoteId)
}

internal fun <T> List<T>.maxCompositeCursorOrNull(cursorOf: (T) -> CompositeSyncCursor?): CompositeSyncCursor? {
    return this.mapNotNull(cursorOf).maxWithOrNull(compareBy<CompositeSyncCursor> { it.updatedAtEpochMillis }.thenBy { it.remoteId })
}
