package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.florent.location.domain.model.TenantStatus
import java.util.UUID

/**
 * Entité Room représentant un locataire en base de données.
 */
@Entity(tableName = "tenants")
data class TenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // NOUVEAU : Pour la synchronisation Supabase
    val remoteId: String = UUID.randomUUID().toString(),

    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val email: String? = null,
    @ColumnInfo(defaultValue = "ACTIVE") val status: String = TenantStatus.ACTIVE.name,

    // NOUVEAU : Timestamps pour la synchronisation
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // NOUVEAU : Flags de synchronisation
    val dirty: Boolean = true, // true = doit être sync avec Supabase
    val serverUpdatedAtEpochSeconds: Long? = null // timestamp serveur
)