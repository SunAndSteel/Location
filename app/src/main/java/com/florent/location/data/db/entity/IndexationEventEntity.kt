package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val leaseId: Long,
    val appliedEpochDay: Long,
    val baseRentCents: Long,
    val indexPercent: Double,
    val newRentCents: Long
)
