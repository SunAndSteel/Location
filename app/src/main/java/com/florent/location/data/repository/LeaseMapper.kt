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
        mailboxLabel = mailboxLabel,
        meterGas = meterGas,
        meterElectricity = meterElectricity,
        meterWater = meterWater,
        indexAnniversaryEpochDay = indexAnniversaryEpochDay
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
        mailboxLabel = mailboxLabel,
        meterGas = meterGas,
        meterElectricity = meterElectricity,
        meterWater = meterWater,
        indexAnniversaryEpochDay = indexAnniversaryEpochDay
    )