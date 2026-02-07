package com.florent.location.data.repository

import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.domain.model.Key

fun Key.toEntity(leaseId: Long, handedOverEpochDay: Long): KeyEntity =
    KeyEntity(
        id = id,
        leaseId = leaseId,
        type = name,
        deviceLabel = description,
        handedOverEpochDay = handedOverEpochDay
    )
