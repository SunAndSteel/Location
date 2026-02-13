package com.florent.location.data.sync

import com.florent.location.data.db.entity.*
import com.florent.location.domain.model.TenantStatus
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
    nowMillis: Long = System.currentTimeMillis()
): TenantEntity {
    val serverCreatedAt = createdAt?.let { Instant.parse(it).toEpochMilli() }
    val serverUpdated = updatedAt?.let { Instant.parse(it).epochSecond }

    return TenantEntity(
        id = localId,
        remoteId = remoteId,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        email = email,
        status = status,
        createdAt = serverCreatedAt ?: nowMillis,
        updatedAt = nowMillis,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdated
    )
}

// ============================================================================
// LEASE MAPPERS
// ============================================================================

/**
 * Convertit une LeaseEntity + les remote IDs du housing et tenant en LeaseRow
 *
 * NOTE: Cette fonction nécessite que vous ayez accès aux remote_id du housing et tenant.
 * Vous devrez les récupérer depuis les DAOs dans le repository.
 */
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

/**
 * Convertit un LeaseRow en LeaseEntity
 *
 * NOTE: Cette fonction nécessite que vous convertissiez les remote_id en local ID.
 * Vous devrez les récupérer depuis les DAOs dans le repository.
 */
fun LeaseRow.toEntityPreservingLocalId(
    localId: Long,
    housingLocalId: Long,
    tenantLocalId: Long,
    nowMillis: Long = System.currentTimeMillis()
): LeaseEntity {
    val serverCreatedAt = createdAt?.let { Instant.parse(it).toEpochMilli() }
    val serverUpdated = updatedAt?.let { Instant.parse(it).epochSecond }

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
        createdAt = serverCreatedAt ?: nowMillis,
        updatedAt = nowMillis,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdated
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
    val serverCreatedAt = createdAt?.let { Instant.parse(it).toEpochMilli() }
    val serverUpdated = updatedAt?.let { Instant.parse(it).epochSecond }

    return KeyEntity(
        id = localId,
        remoteId = remoteId,
        housingId = housingLocalId,
        type = type,
        deviceLabel = deviceLabel,
        handedOverEpochDay = handedOverEpochDay,
        createdAt = serverCreatedAt ?: nowMillis,
        updatedAt = nowMillis,
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
    val serverCreatedAt = createdAt?.let { Instant.parse(it).toEpochMilli() }
    val serverUpdated = updatedAt?.let { Instant.parse(it).epochSecond }

    return IndexationEventEntity(
        id = localId,
        remoteId = remoteId,
        leaseId = leaseLocalId,
        appliedEpochDay = appliedEpochDay,
        baseRentCents = baseRentCents,
        indexPercent = indexPercent,
        newRentCents = newRentCents,
        createdAt = serverCreatedAt ?: nowMillis,
        updatedAt = nowMillis,
        dirty = false,
        serverUpdatedAtEpochSeconds = serverUpdated
    )
}