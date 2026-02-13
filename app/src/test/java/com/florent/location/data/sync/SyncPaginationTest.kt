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
}
