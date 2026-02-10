package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "keys",
    foreignKeys = [
        ForeignKey(
            entity = HousingEntity::class,
            parentColumns = ["id"],
            childColumns = ["housingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["housingId"])]
)
data class KeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // NOUVEAU : Pour la synchronisation Supabase
    val remoteId: String = UUID.randomUUID().toString(),

    val housingId: Long,
    val type: String,
    val deviceLabel: String? = null,
    val handedOverEpochDay: Long,

    // NOUVEAU : Timestamps et sync
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val dirty: Boolean = true,
    val serverUpdatedAtEpochSeconds: Long? = null
)
