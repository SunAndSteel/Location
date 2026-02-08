package com.florent.location.data.repository

import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.domain.model.Lease

fun LeaseEntity.toDomain() : Lease =
    Lease(
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
        housingDepositCentsSnapshot = housingDepositCentsSnapshot
    )


fun Lease.toEntity() : LeaseEntity =
    LeaseEntity(
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
        housingDepositCentsSnapshot = housingDepositCentsSnapshot
    )
