package com.florent.location.data.repository

import com.florent.location.data.db.entity.IndexationEventEntity
import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.data.db.entity.TenantEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncFieldMappersTest {

    @Test
    fun tenant_roundTrip_preservesRemoteIdAndSyncFields() {
        val entity = TenantEntity(
            id = 10L,
            remoteId = "tenant-remote-1",
            firstName = "Alice",
            lastName = "Durand",
            phone = "123",
            email = "alice@example.com",
            status = "ACTIVE",
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_000_001_000,
            dirty = false,
            serverUpdatedAtEpochSeconds = 1_700_000_002
        )

        val roundTrip = entity.toDomain().toEntity()

        assertEquals(entity.remoteId, roundTrip.remoteId)
        assertEquals(entity.createdAt, roundTrip.createdAt)
        assertEquals(entity.updatedAt, roundTrip.updatedAt)
        assertEquals(entity.dirty, roundTrip.dirty)
        assertEquals(entity.serverUpdatedAtEpochSeconds, roundTrip.serverUpdatedAtEpochSeconds)
    }

    @Test
    fun lease_roundTrip_preservesRemoteIdAndSyncFields() {
        val entity = LeaseEntity(
            id = 11L,
            remoteId = "lease-remote-1",
            housingId = 1L,
            tenantId = 2L,
            startDateEpochDay = 20000,
            endDateEpochDay = null,
            rentCents = 100_000,
            chargesCents = 10_000,
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_000_001_000,
            dirty = false,
            serverUpdatedAtEpochSeconds = 1_700_000_002
        )

        val roundTrip = entity.toDomain().toEntity()

        assertEquals(entity.remoteId, roundTrip.remoteId)
        assertEquals(entity.createdAt, roundTrip.createdAt)
        assertEquals(entity.updatedAt, roundTrip.updatedAt)
        assertEquals(entity.dirty, roundTrip.dirty)
        assertEquals(entity.serverUpdatedAtEpochSeconds, roundTrip.serverUpdatedAtEpochSeconds)
    }

    @Test
    fun key_roundTrip_preservesRemoteIdAndSyncFields() {
        val entity = KeyEntity(
            id = 12L,
            remoteId = "key-remote-1",
            housingId = 3L,
            type = "Badge",
            handedOverEpochDay = 20001,
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_000_001_000,
            dirty = false,
            serverUpdatedAtEpochSeconds = 1_700_000_002
        )

        val roundTrip = entity.toDomain().toEntity()

        assertEquals(entity.remoteId, roundTrip.remoteId)
        assertEquals(entity.createdAt, roundTrip.createdAt)
        assertEquals(entity.updatedAt, roundTrip.updatedAt)
        assertEquals(entity.dirty, roundTrip.dirty)
        assertEquals(entity.serverUpdatedAtEpochSeconds, roundTrip.serverUpdatedAtEpochSeconds)
    }

    @Test
    fun indexation_roundTrip_preservesRemoteIdAndSyncFields() {
        val entity = IndexationEventEntity(
            id = 13L,
            remoteId = "idx-remote-1",
            leaseId = 4L,
            appliedEpochDay = 20002,
            baseRentCents = 100_000,
            indexPercent = 2.0,
            newRentCents = 102_000,
            createdAt = 1_700_000_000_000,
            updatedAt = 1_700_000_001_000,
            dirty = false,
            serverUpdatedAtEpochSeconds = 1_700_000_002
        )

        val roundTrip = entity.toDomain().toEntity()

        assertEquals(entity.remoteId, roundTrip.remoteId)
        assertEquals(entity.createdAt, roundTrip.createdAt)
        assertEquals(entity.updatedAt, roundTrip.updatedAt)
        assertEquals(entity.dirty, roundTrip.dirty)
        assertEquals(entity.serverUpdatedAtEpochSeconds, roundTrip.serverUpdatedAtEpochSeconds)
    }

    @Test
    fun newDomainObjects_canGenerateRemoteIdThroughEntityDefaults() {
        val tenantEntity = TenantEntity(id = 1L, firstName = "A", lastName = "B", status = "ACTIVE")

        val mapped = tenantEntity.toDomain().copy(remoteId = "").toEntity()

        assertTrue(mapped.remoteId.isNotBlank())
    }
}
