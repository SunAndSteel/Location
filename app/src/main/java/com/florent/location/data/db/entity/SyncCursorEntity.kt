package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_cursors")
data class SyncCursorEntity(
    @PrimaryKey val syncKey: String,
    val updatedAtEpochMillis: Long,
    val remoteId: String
)

