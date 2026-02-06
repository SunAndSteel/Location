package com.florent.location.data.repository

import com.florent.location.data.db.entity.TenantEntity
import com.florent.location.domain.model.Tenant

fun TenantEntity.toDomain() : Tenant =
    Tenant(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        moveInDateEpochDay = moveInDateEpochDay,
        mailboxLabel = mailboxLabel,
        rentCents = rentCents,
        chargesCents = chargesCents,
        rentDueDayOfMonth = rentDueDayOfMonth
    )


fun Tenant.toEntity(): TenantEntity =
    TenantEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        moveInDateEpochDay = moveInDateEpochDay,
        mailboxLabel = mailboxLabel,
        rentCents = rentCents,
        chargesCents = chargesCents,
        rentDueDayOfMonth = rentDueDayOfMonth
    )