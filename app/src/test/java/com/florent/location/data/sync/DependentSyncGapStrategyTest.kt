package com.florent.location.data.sync

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DependentSyncGapStrategyTest {

    @Test
    fun leaseRow_isInsertedAfterParentBecomesAvailable() = runTest {
        val row = LeaseRow(
            remoteId = "lease-1",
            userId = "user-1",
            housingRemoteId = "housing-1",
            tenantRemoteId = "tenant-1",
            startDateEpochDay = 1,
            rentCents = 100_000,
            chargesCents = 10_000,
            updatedAt = "2024-01-01T00:00:00Z"
        )

        var housingAvailable = false
        var tenantAvailable = false

        val firstPull = mapRowsStoppingAtDependencyGap(
            rows = listOf(row),
            mapRow = { if (housingAvailable && tenantAvailable) it.remoteId else null },
            onMissingDependency = {}
        )
        assertTrue(firstPull.stoppedOnMissingDependency)
        assertTrue(firstPull.mapped.isEmpty())

        housingAvailable = true
        tenantAvailable = true

        val secondPull = mapRowsStoppingAtDependencyGap(
            rows = listOf(row),
            mapRow = { if (housingAvailable && tenantAvailable) it.remoteId else null },
            onMissingDependency = {}
        )
        assertFalse(secondPull.stoppedOnMissingDependency)
        assertEquals(listOf("lease-1"), secondPull.mapped)
    }

    @Test
    fun keyRow_isInsertedAfterParentBecomesAvailable() = runTest {
        val row = KeyRow(
            remoteId = "key-1",
            userId = "user-1",
            housingRemoteId = "housing-1",
            type = "DIGITAL",
            handedOverEpochDay = 1,
            updatedAt = "2024-01-01T00:00:00Z"
        )

        var housingAvailable = false

        val firstPull = mapRowsStoppingAtDependencyGap(
            rows = listOf(row),
            mapRow = { if (housingAvailable) it.remoteId else null },
            onMissingDependency = {}
        )
        assertTrue(firstPull.stoppedOnMissingDependency)
        assertTrue(firstPull.mapped.isEmpty())

        housingAvailable = true

        val secondPull = mapRowsStoppingAtDependencyGap(
            rows = listOf(row),
            mapRow = { if (housingAvailable) it.remoteId else null },
            onMissingDependency = {}
        )
        assertFalse(secondPull.stoppedOnMissingDependency)
        assertEquals(listOf("key-1"), secondPull.mapped)
    }

    @Test
    fun indexationRow_isInsertedAfterParentBecomesAvailable() = runTest {
        val row = IndexationEventRow(
            remoteId = "index-1",
            userId = "user-1",
            leaseRemoteId = "lease-1",
            appliedEpochDay = 1,
            baseRentCents = 100_000,
            indexPercent = 2.0,
            newRentCents = 102_000,
            updatedAt = "2024-01-01T00:00:00Z"
        )

        var leaseAvailable = false

        val firstPull = mapRowsStoppingAtDependencyGap(
            rows = listOf(row),
            mapRow = { if (leaseAvailable) it.remoteId else null },
            onMissingDependency = {}
        )
        assertTrue(firstPull.stoppedOnMissingDependency)
        assertTrue(firstPull.mapped.isEmpty())

        leaseAvailable = true

        val secondPull = mapRowsStoppingAtDependencyGap(
            rows = listOf(row),
            mapRow = { if (leaseAvailable) it.remoteId else null },
            onMissingDependency = {}
        )
        assertFalse(secondPull.stoppedOnMissingDependency)
        assertEquals(listOf("index-1"), secondPull.mapped)
    }
}
