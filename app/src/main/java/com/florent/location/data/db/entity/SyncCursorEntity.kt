package com.florent.location.data.db.entity

import androidx.room.Entity

@Entity(tableName = "sync_cursors", primaryKeys = ["userId", "syncKey"])
data class SyncCursorEntity(
    val userId: String,
    val syncKey: String,
    val updatedAtEpochMillis: Long,
    val remoteId: String
)
