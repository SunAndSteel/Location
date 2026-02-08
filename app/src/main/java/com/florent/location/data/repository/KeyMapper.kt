package com.florent.location.data.repository

import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.domain.model.Key

fun KeyEntity.toDomain(): Key =
    Key(
        id = id,
        housingId = housingId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay
    )

fun Key.toEntity(overrideHousingId: Long? = null): KeyEntity =
    KeyEntity(
        id = id,
        housingId = overrideHousingId ?: housingId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay
    )
