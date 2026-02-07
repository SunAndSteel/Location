package com.florent.location.domain.usecase.lease

import app.cash.turbine.test
import com.florent.location.fake.FakeLeaseRepository
import com.florent.location.fake.FakeLeaseRepository.Companion.ACTIVE_LEASE_ID
import com.florent.location.fake.FakeLeaseRepository.Companion.CLOSE_EPOCH_DAY
import com.florent.location.fake.FakeLeaseRepository.Companion.START_EPOCH_DAY
import com.florent.location.testutils.MainDispatcherRule
import com.florent.location.domain.model.Key
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaseUseCasesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun observeLeaseEmitsUpdatesWhenLeaseChanges() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)

        useCases.observeLease(ACTIVE_LEASE_ID).test {
            val initial = awaitItem()
            assertNotNull(initial)
            assertNull(initial?.endDateEpochDay)

            useCases.closeLease(ACTIVE_LEASE_ID, CLOSE_EPOCH_DAY)
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(CLOSE_EPOCH_DAY, updated?.endDateEpochDay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addKeyAddsKeyAndEmitsUpdatedList() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)

        useCases.observeKeysForLease(ACTIVE_LEASE_ID).test {
            val initial = awaitItem()
            assertEquals(2, initial.size)

            useCases.addKey(
                ACTIVE_LEASE_ID,
                Key(
                    type = "Télécommande",
                    deviceLabel = "Portail",
                    handedOverEpochDay = START_EPOCH_DAY
                )
            )
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(3, updated.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteKeyRemovesKeyAndEmitsUpdatedList() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)

        useCases.observeKeysForLease(ACTIVE_LEASE_ID).test {
            val initial = awaitItem()
            assertEquals(2, initial.size)

            useCases.deleteKey(FakeLeaseRepository.KEY_ID_1)
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(1, updated.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun closeLeaseMarksLeaseInactiveAndEmitsUpdate() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)

        useCases.observeLease(ACTIVE_LEASE_ID).test {
            val initial = awaitItem()
            assertNull(initial?.endDateEpochDay)

            useCases.closeLease(ACTIVE_LEASE_ID, CLOSE_EPOCH_DAY)
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(CLOSE_EPOCH_DAY, updated?.endDateEpochDay)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
