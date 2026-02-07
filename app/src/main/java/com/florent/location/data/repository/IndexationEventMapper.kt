package com.florent.location.data.repository

import com.florent.location.data.db.entity.IndexationEventEntity
import com.florent.location.domain.model.IndexationEvent

fun IndexationEventEntity.toDomain(): IndexationEvent =
    IndexationEvent(
        id = id,
        leaseId = leaseId,
        appliedEpochDay = appliedEpochDay,
        baseRentCents = baseRentCents,
        indexPercent = indexPercent,
        newRentCents = newRentCents
    )

fun IndexationEvent.toEntity(): IndexationEventEntity =
    IndexationEventEntity(
        id = id,
        leaseId = leaseId,
        appliedEpochDay = appliedEpochDay,
        baseRentCents = baseRentCents,
        indexPercent = indexPercent,
        newRentCents = newRentCents
    )
