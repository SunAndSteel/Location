package com.florent.location.data.repository

import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.domain.model.Housing

/**
 * Transforme une entité Room en modèle de domaine.
 */
fun HousingEntity.toDomain(): Housing =
    Housing(
        id = id,
        remoteId = remoteId,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isArchived = isArchived,
        rentCents = rentCents,
        chargesCents = chargesCents,
        depositCents = depositCents,
        mailboxLabel = mailboxLabel,
        meterGasId = meterGasId,
        meterElectricityId = meterElectricityId,
        meterWaterId = meterWaterId,
        pebRating = pebRating,
        pebDate = pebDate,
        buildingLabel = buildingLabel,
        internalNote = internalNote
    )

/**
 * Transforme un modèle de domaine en entité Room.
 */
fun Housing.toEntity(): HousingEntity =
    HousingEntity(
        id = id,
        remoteId = remoteId,
        address = address,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isArchived = isArchived,
        rentCents = rentCents,
        chargesCents = chargesCents,
        depositCents = depositCents,
        mailboxLabel = mailboxLabel,
        meterGasId = meterGasId,
        meterElectricityId = meterElectricityId,
        meterWaterId = meterWaterId,
        pebRating = pebRating,
        pebDate = pebDate,
        buildingLabel = buildingLabel,
        internalNote = internalNote
    )
