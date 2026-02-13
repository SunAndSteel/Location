package com.florent.location.data.sync

import com.florent.location.data.db.entity.*
import java.time.Instant

// ============================================================================
// TENANT MAPPERS
// ============================================================================

fun TenantEntity.toRow(userId: String): TenantRow = TenantRow(
    remoteId = remoteId,
    userId = userId,
    firstName = firstName,
    lastName = lastName,
    phone = phone,
    email = email,
    status = status,
    createdAt = Instant.ofEpochMilli(createdAt).toString()
)

fun TenantRow.toEntityPreservingLocalId(
    localId: Long,
    existingCreatedAtMillis: Long? = null,
    nowMillis: Long = System.currentTimeMillis()
): TenantEntity {
    val serverCreatedAtMillis = parseServerEpochMillis(createdAt)
    val serverUpdatedAtMillis = parseServerEpochMillis(updatedAt)

    return TenantEntity(
        id = localId,
        remoteId = remoteId,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        status = status,
        createdAt = serverCreatedAtMillis ?: existingCreatedAtMillis ?: nowMillis,
        updatedAt = serverUpdatedAtMillis ?: nowMillis,
        isDeleted = false,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdatedAtMillis
    )
}

// ============================================================================
// LEASE MAPPERS
// ============================================================================

fun LeaseEntity.toRow(
    userId: String,
    housingRemoteId: String,
    tenantRemoteId: String
): LeaseRow = LeaseRow(
    remoteId = remoteId,
    userId = userId,
    housingRemoteId = housingRemoteId,
    tenantRemoteId = tenantRemoteId,
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
    createdAt = Instant.ofEpochMilli(createdAt).toString()
)

fun LeaseRow.toEntityPreservingLocalId(
    localId: Long,
    housingLocalId: Long,
    tenantLocalId: Long,
    existingCreatedAtMillis: Long? = null,
    nowMillis: Long = System.currentTimeMillis()
): LeaseEntity {
    val serverCreatedAtMillis = parseServerEpochMillis(createdAt)
    val serverUpdatedAtMillis = parseServerEpochMillis(updatedAt)

    return LeaseEntity(
        id = localId,
        remoteId = remoteId,
        housingId = housingLocalId,
        tenantId = tenantLocalId,
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
        createdAt = serverCreatedAtMillis ?: existingCreatedAtMillis ?: nowMillis,
        updatedAt = serverUpdatedAtMillis ?: nowMillis,
        isDeleted = false,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdatedAtMillis
    )
}

// ============================================================================
// KEY MAPPERS
// ============================================================================

fun KeyEntity.toRow(
    userId: String,
    housingRemoteId: String
): KeyRow = KeyRow(
    remoteId = remoteId,
    userId = userId,
    housingRemoteId = housingRemoteId,
    type = type,
    deviceLabel = deviceLabel,
    handedOverEpochDay = handedOverEpochDay,
    createdAt = Instant.ofEpochMilli(createdAt).toString()
)

fun KeyRow.toEntityPreservingLocalId(
    localId: Long,
    housingLocalId: Long,
    nowMillis: Long = System.currentTimeMillis()
): KeyEntity {
    val serverCreatedAt = parseServerEpochMillis(createdAt)
    val serverUpdatedAtMillis = parseServerEpochMillis(updatedAt)
    val serverUpdated = parseServerEpochMillis(updatedAt)

    return KeyEntity(
        id = localId,
        remoteId = remoteId,
        housingId = housingLocalId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay,
        createdAt = serverCreatedAt ?: nowMillis,
        updatedAt = serverUpdatedAtMillis ?: nowMillis,
        isDeleted = false,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdated
    )
}

// ============================================================================
// INDEXATION EVENT MAPPERS
// ============================================================================

fun IndexationEventEntity.toRow(
    userId: String,
    leaseRemoteId: String
): IndexationEventRow = IndexationEventRow(
    remoteId = remoteId,
    userId = userId,
    leaseRemoteId = leaseRemoteId,
    appliedEpochDay = appliedEpochDay,
    baseRentCents = baseRentCents,
    indexPercent = indexPercent,
    newRentCents = newRentCents,
    createdAt = Instant.ofEpochMilli(createdAt).toString()
)

fun IndexationEventRow.toEntityPreservingLocalId(
    localId: Long,
    leaseLocalId: Long,
    nowMillis: Long = System.currentTimeMillis()
): IndexationEventEntity {
    val serverCreatedAt = parseServerEpochMillis(createdAt)
    val serverUpdatedAtMillis = parseServerEpochMillis(updatedAt)
    val serverUpdated = parseServerEpochMillis(updatedAt)

    return IndexationEventEntity(
        id = localId,
        remoteId = remoteId,
        leaseId = leaseLocalId,
        appliedEpochDay = appliedEpochDay,
        baseRentCents = baseRentCents,
        indexPercent = indexPercent,
        newRentCents = newRentCents,
        createdAt = serverCreatedAt ?: nowMillis,
        updatedAt = serverUpdatedAtMillis ?: nowMillis,
        isDeleted = false,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdated
    )
}
