package com.florent.location.data.db.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.florent.location.domain.model.PebRating
import java.util.UUID

@Entity(
    tableName = "housings",
    indices = [
        Index(value = ["remoteId"], unique = true),
        Index(value = ["isArchived"]),
        Index(value = ["addr_city", "addr_zipCode"])
    ]
)
data class HousingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val remoteId: String = UUID.randomUUID().toString(),
    @Embedded(prefix = "addr_") val address: AddressEntity,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val rentCents: Long = 0L,
    val chargesCents: Long = 0L,
    val depositCents: Long = 0L,
    val meterGasId: String? = null,
    val meterElectricityId: String? = null,
    val meterWaterId: String? = null,
    val mailboxLabel: String? = null,
    val pebRating: PebRating = PebRating.UNKNOWN,
    val pebDate: String? = null,
    val buildingLabel: String? = null,
    val internalNote: String? = null,
    val isDeleted: Boolean = false,
    val dirty: Boolean = true,
    val serverUpdatedAtEpochSeconds: Long? = null

)
