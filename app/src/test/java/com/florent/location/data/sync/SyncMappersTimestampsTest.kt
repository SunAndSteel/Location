package com.florent.location.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SyncMappersTimestampsTest {

    @Test
    fun `maps server created_at and updated_at when present`() {
        val createdAtIso = "2024-01-01T10:00:00Z"
        val updatedAtIso = "2024-01-02T11:30:00Z"

        val housingEntity = housingRow(createdAt = createdAtIso, updatedAt = updatedAtIso)
            .toEntityPreservingLocalId(localId = 1L, nowMillis = 123L)
        val tenantEntity = tenantRow(createdAt = createdAtIso, updatedAt = updatedAtIso)
            .toEntityPreservingLocalId(localId = 1L, nowMillis = 123L)
        val leaseEntity = leaseRow(createdAt = createdAtIso, updatedAt = updatedAtIso)
            .toEntityPreservingLocalId(localId = 1L, housingLocalId = 10L, tenantLocalId = 20L, nowMillis = 123L)

        val expectedCreatedAt = parseServerEpochMillis(createdAtIso)
        val expectedUpdatedAt = parseServerEpochMillis(updatedAtIso)
        val expectedUpdatedMillis = parseServerEpochMillis(updatedAtIso)

        assertEquals(expectedCreatedAt, housingEntity.createdAt)
        assertEquals(expectedUpdatedAt, housingEntity.updatedAt)
        assertEquals(expectedUpdatedMillis, housingEntity.serverUpdatedAtEpochSeconds)

        assertEquals(expectedCreatedAt, tenantEntity.createdAt)
        assertEquals(expectedUpdatedAt, tenantEntity.updatedAt)
        assertEquals(expectedUpdatedMillis, tenantEntity.serverUpdatedAtEpochSeconds)

        assertEquals(expectedCreatedAt, leaseEntity.createdAt)
        assertEquals(expectedUpdatedAt, leaseEntity.updatedAt)
        assertEquals(expectedUpdatedMillis, leaseEntity.serverUpdatedAtEpochSeconds)
    }

    @Test
    fun `keeps existing createdAt on update when server created_at is missing`() {
        val existingCreatedAt = 999L
        val nowMillis = 1234L

        val housingEntity = housingRow(createdAt = null, updatedAt = null)
            .toEntityPreservingLocalId(localId = 1L, existingCreatedAtMillis = existingCreatedAt, nowMillis = nowMillis)
        val tenantEntity = tenantRow(createdAt = null, updatedAt = null)
            .toEntityPreservingLocalId(localId = 1L, existingCreatedAtMillis = existingCreatedAt, nowMillis = nowMillis)
        val leaseEntity = leaseRow(createdAt = null, updatedAt = null)
            .toEntityPreservingLocalId(
                localId = 1L,
                housingLocalId = 10L,
                tenantLocalId = 20L,
                existingCreatedAtMillis = existingCreatedAt,
                nowMillis = nowMillis
            )

        assertEquals(existingCreatedAt, housingEntity.createdAt)
        assertEquals(nowMillis, housingEntity.updatedAt)
        assertNull(housingEntity.serverUpdatedAtEpochSeconds)

        assertEquals(existingCreatedAt, tenantEntity.createdAt)
        assertEquals(nowMillis, tenantEntity.updatedAt)
        assertNull(tenantEntity.serverUpdatedAtEpochSeconds)

        assertEquals(existingCreatedAt, leaseEntity.createdAt)
        assertEquals(nowMillis, leaseEntity.updatedAt)
        assertNull(leaseEntity.serverUpdatedAtEpochSeconds)
    }

    @Test
    fun `falls back safely when server timestamp format is invalid`() {
        val invalidTimestamp = "not-a-date"
        val nowMillis = 4321L

        val housingEntity = housingRow(createdAt = invalidTimestamp, updatedAt = invalidTimestamp)
            .toEntityPreservingLocalId(localId = 1L, existingCreatedAtMillis = 111L, nowMillis = nowMillis)
        val tenantEntity = tenantRow(createdAt = invalidTimestamp, updatedAt = invalidTimestamp)
            .toEntityPreservingLocalId(localId = 1L, existingCreatedAtMillis = 222L, nowMillis = nowMillis)
        val leaseEntity = leaseRow(createdAt = invalidTimestamp, updatedAt = invalidTimestamp)
            .toEntityPreservingLocalId(
                localId = 1L,
                housingLocalId = 10L,
                tenantLocalId = 20L,
                existingCreatedAtMillis = 333L,
                nowMillis = nowMillis
            )

        assertEquals(111L, housingEntity.createdAt)
        assertEquals(nowMillis, housingEntity.updatedAt)
        assertNull(housingEntity.serverUpdatedAtEpochSeconds)

        assertEquals(222L, tenantEntity.createdAt)
        assertEquals(nowMillis, tenantEntity.updatedAt)
        assertNull(tenantEntity.serverUpdatedAtEpochSeconds)

        assertEquals(333L, leaseEntity.createdAt)
        assertEquals(nowMillis, leaseEntity.updatedAt)
        assertNull(leaseEntity.serverUpdatedAtEpochSeconds)
    }


    @Test
    fun `keeps millisecond precision for server updated cursor`() {
        val firstUpdateIso = "2024-01-02T11:30:00.100Z"
        val secondUpdateIso = "2024-01-02T11:30:00.900Z"

        val firstEntity = housingRow(createdAt = firstUpdateIso, updatedAt = firstUpdateIso)
            .toEntityPreservingLocalId(localId = 1L, nowMillis = 123L)
        val secondEntity = housingRow(createdAt = secondUpdateIso, updatedAt = secondUpdateIso)
            .toEntityPreservingLocalId(localId = 2L, nowMillis = 123L)

        val firstCursor = toServerCursorIso(firstEntity.serverUpdatedAtEpochSeconds)
        val secondCursor = toServerCursorIso(secondEntity.serverUpdatedAtEpochSeconds)

        assertEquals(parseServerEpochMillis(firstUpdateIso), firstEntity.serverUpdatedAtEpochSeconds)
        assertEquals(parseServerEpochMillis(secondUpdateIso), secondEntity.serverUpdatedAtEpochSeconds)
        assertEquals(firstUpdateIso, firstCursor)
        assertEquals(secondUpdateIso, secondCursor)
    }

    @Test
    fun `keeps backward compatibility for second-based cursor values`() {
        val legacySecondsCursor = 1704195000L

        assertEquals("2024-01-02T11:30:00Z", toServerCursorIso(legacySecondsCursor))
    }
    private fun housingRow(createdAt: String?, updatedAt: String?) = HousingRow(
        remoteId = "h-1",
        userId = "u-1",
        addrStreet = "Rue",
        addrNumber = "10",
        addrZipCode = "1000",
        addrCity = "Bruxelles",
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun tenantRow(createdAt: String?, updatedAt: String?) = TenantRow(
        remoteId = "t-1",
        userId = "u-1",
        firstName = "Jane",
        lastName = "Doe",
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun leaseRow(createdAt: String?, updatedAt: String?) = LeaseRow(
        remoteId = "l-1",
        userId = "u-1",
        housingRemoteId = "h-1",
        tenantRemoteId = "t-1",
        startDateEpochDay = 1L,
        rentCents = 1000L,
        chargesCents = 200L,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
