package com.florent.location.data.repository

import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.domain.model.Key

fun KeyEntity.toDomain(): Key =
    Key(
        id = id,
        leaseId = leaseId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay
    )

fun Key.toEntity(overrideLeaseId: Long? = null): KeyEntity =
    KeyEntity(
        id = id,
        leaseId = overrideLeaseId ?: leaseId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay
    )
