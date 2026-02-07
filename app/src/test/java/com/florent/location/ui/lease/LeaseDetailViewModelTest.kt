package com.florent.location.ui.lease

import app.cash.turbine.test
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.fake.FakeLeaseRepository
import com.florent.location.fake.FakeLeaseRepository.Companion.ACTIVE_LEASE_ID
import com.florent.location.fake.FakeLeaseRepository.Companion.CLOSE_EPOCH_DAY
import com.florent.location.fake.FakeLeaseRepository.Companion.START_EPOCH_DAY
import com.florent.location.testutils.MainDispatcherRule
import com.florent.location.domain.usecase.lease.LeaseUseCasesImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaseDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoadShowsLeaseAndKeys() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)
        val viewModel = LeaseDetailViewModel(ACTIVE_LEASE_ID, useCases)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertNotNull(loaded.lease)
            assertEquals(2, loaded.keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addKeyEventUpdatesKeysList() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)
        val viewModel = LeaseDetailViewModel(ACTIVE_LEASE_ID, useCases)

        advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem()
            val initialKeys = if (initial.isLoading) {
                val next = awaitItem()
                next
            } else {
                initial
            }
            assertEquals(2, initialKeys.keys.size)

            viewModel.onEvent(
                LeaseDetailUiEvent.ConfirmAddKey(
                    type = "Badge",
                    deviceLabel = "Portail",
                    handedOverDate = epochDayToDate(START_EPOCH_DAY)
                )
            )
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(3, updated.keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteKeyEventUpdatesKeysList() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)
        val viewModel = LeaseDetailViewModel(ACTIVE_LEASE_ID, useCases)

        advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem()
            val initialKeys = if (initial.isLoading) {
                val next = awaitItem()
                next
            } else {
                initial
            }
            assertEquals(2, initialKeys.keys.size)

            viewModel.onEvent(LeaseDetailUiEvent.DeleteKeyClicked(FakeLeaseRepository.KEY_ID_1))
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(1, updated.keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun closeLeaseEventMarksLeaseInactive() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)
        val viewModel = LeaseDetailViewModel(ACTIVE_LEASE_ID, useCases)

        advanceUntilIdle()

        viewModel.uiState.test {
            val initial = awaitItem()
            val initialLeaseState = if (initial.isLoading) {
                val next = awaitItem()
                next
            } else {
                initial
            }
            assertTrue(initialLeaseState.isActive)

            viewModel.onEvent(LeaseDetailUiEvent.ConfirmCloseLease(epochDayToDate(CLOSE_EPOCH_DAY)))
            advanceUntilIdle()

            val updated = awaitItem()
            assertFalse(updated.isActive)
            assertEquals(CLOSE_EPOCH_DAY, updated.lease?.endDateEpochDay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun missingLeaseIdShowsEmptyStateWithoutCrash() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val useCases = LeaseUseCasesImpl(repository)
        val viewModel = LeaseDetailViewModel(999L, useCases)

        viewModel.uiState.test {
            val loading = awaitItem()
            assertTrue(loading.isLoading)

            advanceUntilIdle()

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertNull(loaded.lease)
            assertEquals(0, loaded.keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeLeaseFailureUpdatesErrorMessage() = runTest {
        val viewModel = LeaseDetailViewModel(
            ACTIVE_LEASE_ID,
            FakeLeaseUseCases(
                flow {
                    throw IllegalStateException("boom")
                }
            )
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("boom", state.errorMessage)
    }

    private class FakeLeaseUseCases(
        private val leaseFlow: Flow<Lease?>
    ) : LeaseUseCases {
        override suspend fun createLease(request: com.florent.location.domain.usecase.lease.LeaseCreateRequest): Long = 0L

        override fun observeLease(leaseId: Long): Flow<Lease?> = leaseFlow

        override fun observeKeysForLease(leaseId: Long): Flow<List<Key>> = flowOf(emptyList())

        override suspend fun addKey(leaseId: Long, key: Key): Long = 0L

        override suspend fun deleteKey(keyId: Long) = Unit

        override suspend fun closeLease(leaseId: Long, endEpochDay: Long) = Unit
    }

    private fun epochDayToDate(epochDay: Long): String {
        return LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
