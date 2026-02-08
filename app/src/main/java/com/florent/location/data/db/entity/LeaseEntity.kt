package com.florent.location.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
        tableName = "leases",
        foreignKeys =
                [
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
                        )],
        indices =
                [
                        Index(value = ["housingId"]),
                        Index(value = ["tenantId"]),
                        Index(value = ["housingId", "endDateEpochDay"]) // utile pour bail actif
                ]
)
data class LeaseEntity(
        @PrimaryKey(autoGenerate = true) val id: Long = 0L,
        val housingId: Long,
        val tenantId: Long,
        val startDateEpochDay: Long, // LocalDate.toEpochDay()
        val endDateEpochDay: Long? = null, // null = actif
        val rentCents: Long,
        val chargesCents: Long,
        val depositCents: Long = 0L,
        val rentDueDayOfMonth: Int = 1,
        val indexAnniversaryEpochDay: Long? = null // par défaut startDateEpochDay
)
