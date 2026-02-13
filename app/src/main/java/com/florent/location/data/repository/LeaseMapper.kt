package com.florent.location.data.repository

import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.domain.model.Lease

fun LeaseEntity.toDomain() : Lease =
    Lease(
        id = id,
        remoteId = remoteId,
        housingId = housingId,
        tenantId = tenantId,
        startDateEpochDay = startDateEpochDay,
        endDateEpochDay = endDateEpochDay,
        rentCents = rentCents,
        chargesCents = chargesCents,
        depositCents = depositCents,
        rentDueDayOfMonth = rentDueDayOfMonth,
        indexAnniversaryEpochDay = indexAnniversaryEpochDay,
        rentOverridden = rentOverridden,
        chargesOverridden = chargesOverridden,
        depositOverridden = depositOverridden,
        housingRentCentsSnapshot = housingRentCentsSnapshot,
        housingChargesCentsSnapshot = housingChargesCentsSnapshot,
        housingDepositCentsSnapshot = housingDepositCentsSnapshot,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochSeconds = serverUpdatedAtEpochSeconds
    )


fun Lease.toEntity() : LeaseEntity {
    val baseEntity = LeaseEntity(
        id = id,
        housingId = housingId,
        tenantId = tenantId,
        startDateEpochDay = startDateEpochDay,
        endDateEpochDay = endDateEpochDay,
        rentCents = rentCents,
        chargesCents = chargesCents,
        depositCents = depositCents,
        rentDueDayOfMonth = rentDueDayOfMonth,
        indexAnniversaryEpochDay = indexAnniversaryEpochDay,
        rentOverridden = rentOverridden,
        chargesOverridden = chargesOverridden,
        depositOverridden = depositOverridden,
        housingRentCentsSnapshot = housingRentCentsSnapshot,
        housingChargesCentsSnapshot = housingChargesCentsSnapshot,
        housingDepositCentsSnapshot = housingDepositCentsSnapshot,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dirty = dirty,
        serverUpdatedAtEpochSeconds = serverUpdatedAtEpochSeconds
    )

    return if (remoteId.isBlank()) baseEntity else baseEntity.copy(remoteId = remoteId)
}
