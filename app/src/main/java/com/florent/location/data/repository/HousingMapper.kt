package com.florent.location.data.repository

import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.domain.model.Housing

/**
 * Transforme une entité Room en modèle de domaine.
 */
fun HousingEntity.toDomain(): Housing =
    Housing(
        id = id,
        city = city,
        address = address,
        defaultRentCents = defaultRentCents,
        defaultChargesCents = defaultChargesCents,
        depositCents = depositCents,
        peb = peb,
        buildingLabel = buildingLabel
    )

/**
 * Transforme un modèle de domaine en entité Room.
 */
fun Housing.toEntity(): HousingEntity =
    HousingEntity(
        id = id,
        city = city,
        address = address,
        defaultRentCents = defaultRentCents,
        defaultChargesCents = defaultChargesCents,
        depositCents = depositCents,
        peb = peb,
        buildingLabel = buildingLabel
    )
