package com.florent.location.data.repository

import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.domain.model.Key

fun KeyEntity.toDomain(): Key =
    Key(
        id = id,
        remoteId = remoteId,
        housingId = housingId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochSeconds = serverUpdatedAtEpochSeconds
    )

fun Key.toEntity(overrideHousingId: Long? = null): KeyEntity {
    val baseEntity = KeyEntity(
        id = id,
        housingId = overrideHousingId ?: housingId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochSeconds = serverUpdatedAtEpochSeconds
    )

    return if (remoteId.isBlank()) baseEntity else baseEntity.copy(remoteId = remoteId)
}
