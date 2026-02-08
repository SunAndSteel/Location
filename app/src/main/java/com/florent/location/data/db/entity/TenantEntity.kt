package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.florent.location.domain.model.TenantStatus

/**
 * Entité Room représentant un locataire en base de données.
 */
@Entity(tableName = "tenants")
data class TenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val email: String? = null,
    @ColumnInfo(defaultValue = "ACTIVE") val status: String = TenantStatus.ACTIVE.name
)
