package com.florent.location.data.sync

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncPaginationTest {

    @Test
    fun fetchAllPaged_collectsAllRowsAcrossTwoPages() = runTest {
        val remoteRows = (0 until 1500).map { "remote-$it" }

        val fetched = fetchAllPaged(
            tag = "SyncPaginationTest",
            pageLabel = "rows",
            pageSize = 1000,
            fetchPage = { from, to ->
                remoteRows.subList(from.coerceAtMost(remoteRows.size), (to + 1).coerceAtMost(remoteRows.size))
            }
        )

        assertEquals(1500, fetched.size)
        assertEquals(remoteRows, fetched)
    }

    @Test
    fun fetchAllPaged_preventsAbusiveLocalDeletionWhenDataSpansTwoPages() = runTest {
        val remoteRows = (0 until 1500).map { "remote-$it" }
        val localIds = remoteRows + "stale-remote"

        val fetchedRemoteIds = fetchAllPaged(
            tag = "SyncPaginationTest",
            pageLabel = "remoteIds",
            pageSize = 1000,
            fetchPage = { from, to ->
                remoteRows.subList(from.coerceAtMost(remoteRows.size), (to + 1).coerceAtMost(remoteRows.size))
            }
        ).toSet()

        val toDelete = localIds.filterNot { fetchedRemoteIds.contains(it) }

        assertEquals(listOf("stale-remote"), toDelete)
        assertTrue(toDelete.none { it.startsWith("remote-") })
    }

    @Test
    fun processKeysetPagedWithMetrics_keepsRowsInsertedOrUpdatedBetweenPages() = runTest {
        data class RemoteRow(val remoteId: String, val updatedAt: String)

        val remoteRows = mutableListOf(
            RemoteRow("A", "2024-01-01T00:00:00Z"),
            RemoteRow("B", "2024-01-01T00:00:01Z"),
            RemoteRow("C", "2024-01-01T00:00:02Z"),
            RemoteRow("D", "2024-01-01T00:00:03Z")
        )

        var fetchCount = 0
        val appliedRemoteIds = mutableListOf<String>()

        val result = processKeysetPagedWithMetrics(
            tag = "SyncPaginationTest",
            pageLabel = "incremental",
            initialCursor = null,
            pageSize = 2,
            fetchPage = { sinceIso, limit ->
                fetchCount++
                if (fetchCount == 2) {
                    remoteRows += RemoteRow("B-NEW", "2024-01-01T00:00:01Z")
                    val index = remoteRows.indexOfFirst { it.remoteId == "A" }
                    remoteRows[index] = RemoteRow("A", "2024-01-01T00:00:04Z")
                }

                remoteRows
                    .filter { row -> sinceIso == null || row.updatedAt >= sinceIso }
                    .sortedWith(compareBy<RemoteRow> { it.updatedAt }.thenBy { it.remoteId })
                    .take(limit)
            },
            extractUpdatedAt = { it.updatedAt },
            extractRemoteId = { it.remoteId },
            processPage = { rows -> appliedRemoteIds += rows.map { it.remoteId } },
            onCursorAdvanced = {}
        )

        assertEquals(6, result.processedCount)
        assertEquals(listOf("A", "B", "B-NEW", "C", "D", "A"), appliedRemoteIds)
    }
}
