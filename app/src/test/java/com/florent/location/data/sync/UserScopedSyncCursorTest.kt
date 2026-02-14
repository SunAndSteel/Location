package com.florent.location.data.sync

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserScopedSyncCursorTest {

    @Test
    fun `switch user A to B does not reuse cursor and skip B remote rows`() {
        val cursorFromUserA = CompositeSyncCursor(
            updatedAtEpochMillis = 1_704_195_000_000,
            remoteId = "tenant-999"
        )

        val remoteRowTimestamp = "2024-01-02T11:30:00Z"
        val firstRemoteRowForUserB = "tenant-001"

        assertFalse(
            "Using A cursor would wrongly skip B row when updated_at is identical and remote_id is lower",
            isAfterCursor(remoteRowTimestamp, firstRemoteRowForUserB, cursorFromUserA)
        )

        val cursorForUserB: CompositeSyncCursor? = null
        assertTrue(
            "With user-scoped cursor, B starts without cursor and does not skip remote rows",
            isAfterCursor(remoteRowTimestamp, firstRemoteRowForUserB, cursorForUserB)
        )
    }
}
