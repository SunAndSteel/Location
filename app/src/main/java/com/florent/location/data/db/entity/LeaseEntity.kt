package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "leases",
    foreignKeys = [
        ForeignKey(
            entity = HousingEntity::class,
            parentColumns = ["id"],
            childColumns = ["housingId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = TenantEntity::class,
            parentColumns = ["id"],
            childColumns = ["tenantId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["housingId"]),
        Index(value = ["tenantId"]),
        Index(value = ["housingId", "endDateEpochDay"])
    ]
)
data class LeaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    // NOUVEAU : Pour la synchronisation Supabase
    val remoteId: String = UUID.randomUUID().toString(),

    val housingId: Long,
    val tenantId: Long,
    val startDateEpochDay: Long,
    val endDateEpochDay: Long? = null,
    val rentCents: Long,
    val chargesCents: Long,
    val depositCents: Long = 0L,
    val rentDueDayOfMonth: Int = 1,
    val indexAnniversaryEpochDay: Long? = null,
    val rentOverridden: Boolean = false,
    val chargesOverridden: Boolean = false,
    val depositOverridden: Boolean = false,
    val housingRentCentsSnapshot: Long = 0L,
    val housingChargesCentsSnapshot: Long = 0L,
    val housingDepositCentsSnapshot: Long = 0L,

    // NOUVEAU : Timestamps et sync
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val dirty: Boolean = true,
    val serverUpdatedAtEpochMillis: Long? = null
)