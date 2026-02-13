package com.florent.location.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositeSyncCursorTest {

    @Test
    fun `isAfterCursor only accepts greater remote id when updated_at is identical`() {
        val cursor = CompositeSyncCursor(
            updatedAtEpochMillis = 1_704_195_000_000,
            remoteId = "remote-002"
        )

        assertFalse(isAfterCursor("2024-01-02T11:30:00Z", "remote-001", cursor))
        assertFalse(isAfterCursor("2024-01-02T11:30:00Z", "remote-002", cursor))
        assertTrue(isAfterCursor("2024-01-02T11:30:00Z", "remote-003", cursor))
    }

    @Test
    fun `maxCompositeCursorOrNull picks max remote id for identical updated_at`() {
        val rows = listOf(
            CompositeSyncCursor(1_704_195_000_000, "remote-001"),
            CompositeSyncCursor(1_704_195_000_000, "remote-003"),
            CompositeSyncCursor(1_704_195_000_000, "remote-002")
        )

        val max = rows.maxCompositeCursorOrNull { it }

        assertEquals("remote-003", max?.remoteId)
        assertEquals(1_704_195_000_000, max?.updatedAtEpochMillis)
    }
}
