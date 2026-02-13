package com.florent.location.data.repository

import com.florent.location.data.db.entity.TenantEntity
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.model.TenantStatus

/**
 * Transforme une entité Room en modèle de domaine.
 */
fun TenantEntity.toDomain() : Tenant =
    Tenant(
        id = id,
        remoteId = remoteId,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        status = TenantStatus.entries.firstOrNull { it.name == status } ?: TenantStatus.INACTIVE,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochMillis = serverUpdatedAtEpochMillis
    )


/**
 * Transforme un modèle de domaine en entité Room.
 */
fun Tenant.toEntity(): TenantEntity {
    val baseEntity = TenantEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochMillis = serverUpdatedAtEpochMillis
    )

    return if (remoteId.isBlank()) baseEntity else baseEntity.copy(remoteId = remoteId)
}
