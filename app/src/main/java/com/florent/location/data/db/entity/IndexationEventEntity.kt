package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "indexation_events",
    foreignKeys = [
        ForeignKey(
            entity = LeaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["leaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["leaseId"])]
)
data class IndexationEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // NOUVEAU : Pour la synchronisation Supabase
    val remoteId: String = UUID.randomUUID().toString(),

    val leaseId: Long,
    val appliedEpochDay: Long,
    val baseRentCents: Long,
    val indexPercent: Double,
    val newRentCents: Long,

    // NOUVEAU : Timestamps et sync
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val dirty: Boolean = true,
    val serverUpdatedAtEpochMillis: Long? = null
)
