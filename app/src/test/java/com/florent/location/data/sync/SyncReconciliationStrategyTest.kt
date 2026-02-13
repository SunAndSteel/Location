package com.florent.location.data.sync

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncReconciliationStrategyTest {

    @Test
    fun policy_runsFullReconciliationOnFirstSync_thenSkipsUntilInterval() {
        var nowMs = 0L
        val policy = SyncDeletionReconciliationPolicy(
            fullReconciliationIntervalMs = 1_000L,
            nowProvider = { nowMs }
        )

        assertTrue(policy.shouldRunFullReconciliation("TenantSyncRepository"))

        nowMs = 500L
        assertFalse(policy.shouldRunFullReconciliation("TenantSyncRepository"))

        nowMs = 1_000L
        assertTrue(policy.shouldRunFullReconciliation("TenantSyncRepository"))
    }

    @Test
    fun policy_canForceManualFullReconciliation() {
        var nowMs = 0L
        val policy = SyncDeletionReconciliationPolicy(
            fullReconciliationIntervalMs = 10_000L,
            nowProvider = { nowMs }
        )

        assertTrue(policy.shouldRunFullReconciliation("LeaseSyncRepository"))

        nowMs = 1_000L
        assertFalse(policy.shouldRunFullReconciliation("LeaseSyncRepository"))

        policy.forceNextFullReconciliation("LeaseSyncRepository")
        assertTrue(policy.shouldRunFullReconciliation("LeaseSyncRepository"))
    }

    @Test
    fun fetchAllPagedWithMetrics_handlesLargeVolumeWithoutMissingRows() = runTest {
        val remoteRows = (0 until 12_500).map { "remote-$it" }

        val result = fetchAllPagedWithMetrics(
            tag = "SyncReconciliationStrategyTest",
            pageLabel = "largeVolume",
            pageSize = 1_000,
            fetchPage = { from, to ->
                remoteRows.subList(from.coerceAtMost(remoteRows.size), (to + 1).coerceAtMost(remoteRows.size))
            }
        )

        assertEquals(12_500, result.rows.size)
        assertEquals(13, result.pageCount)
        assertEquals(remoteRows, result.rows)
    }
}
