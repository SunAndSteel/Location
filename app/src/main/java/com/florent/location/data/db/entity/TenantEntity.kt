package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

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

    val moveInDateEpochDay: Long? = null, // LocalDate.toEpochDay()
    val mailboxLabel: String? = null,

    val rentCents: Long = 0L,
    val chargesCents: Long = 0L,
    val rentDueDayOfMonth: Int = 1
)
