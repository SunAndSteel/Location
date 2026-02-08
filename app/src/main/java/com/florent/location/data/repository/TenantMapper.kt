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
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        status = TenantStatus.entries.firstOrNull { it.name == status } ?: TenantStatus.INACTIVE
    )


/**
 * Transforme un modèle de domaine en entité Room.
 */
fun Tenant.toEntity(): TenantEntity =
    TenantEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        status = status.name
    )
