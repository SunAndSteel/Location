package com.florent.location.data.db.model

import androidx.room.Embedded
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.data.db.entity.LeaseEntity

data class HousingWithActiveLease(
    @Embedded val housing: HousingEntity,
    @Embedded(prefix = "lease_") val activeLease: LeaseEntity?
)
