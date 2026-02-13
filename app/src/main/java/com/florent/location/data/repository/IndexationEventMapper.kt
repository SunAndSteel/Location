package com.florent.location.data.repository

import com.florent.location.data.db.entity.IndexationEventEntity
import com.florent.location.domain.model.IndexationEvent

fun IndexationEventEntity.toDomain(): IndexationEvent =
    IndexationEvent(
        id = id,
        remoteId = remoteId,
        leaseId = leaseId,
        appliedEpochDay = appliedEpochDay,
        baseRentCents = baseRentCents,
        indexPercent = indexPercent,
        newRentCents = newRentCents,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochMillis = serverUpdatedAtEpochMillis
    )

fun IndexationEvent.toEntity(): IndexationEventEntity {
    val baseEntity = IndexationEventEntity(
        id = id,
        leaseId = leaseId,
        appliedEpochDay = appliedEpochDay,
        baseRentCents = baseRentCents,
        indexPercent = indexPercent,
        newRentCents = newRentCents,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochMillis = serverUpdatedAtEpochMillis
    )

    return if (remoteId.isBlank()) baseEntity else baseEntity.copy(remoteId = remoteId)
}
