package com.florent.location.ui.lease

import app.cash.turbine.test
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Lease
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.bail.BailUseCases
import com.florent.location.domain.usecase.bail.BailUseCasesImpl
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.housing.HousingUseCasesImpl
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCasesImpl
import com.florent.location.fake.FakeLeaseRepository
import com.florent.location.fake.FakeHousingRepository
import com.florent.location.fake.FakeTenantRepository
import com.florent.location.fake.FakeLeaseRepository.Companion.ACTIVE_LEASE_ID
import com.florent.location.fake.FakeLeaseRepository.Companion.ACTIVE_HOUSING_ID
import com.florent.location.fake.FakeLeaseRepository.Companion.ACTIVE_TENANT_ID
import com.florent.location.fake.FakeLeaseRepository.Companion.CLOSE_EPOCH_DAY
import com.florent.location.fake.FakeLeaseRepository.Companion.START_EPOCH_DAY
import com.florent.location.sampleHousing
import com.florent.location.testutils.RecordingSyncRequester
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

    private val tenantUseCases = TenantUseCasesImpl(
        FakeTenantRepository(
            listOf(
                Tenant(
                    id = ACTIVE_TENANT_ID,
                    firstName = "Alice",
                    lastName = "Durand",
                    phone = null,
                    email = null
                )
            )
        )
    )

    @Test
    fun initialLoadShowsLeaseAndKeys() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val bailUseCases = BailUseCasesImpl(repository)
        val leaseUseCases = LeaseUseCasesImpl(repository, FakeHousingRepository())
        val housingUseCases = buildHousingUseCases()
        val viewModel = LeaseDetailViewModel(
            ACTIVE_LEASE_ID,
            bailUseCases,
            leaseUseCases,
            housingUseCases,
            tenantUseCases,
            RecordingSyncRequester()
        )

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
        val bailUseCases = BailUseCasesImpl(repository)
        val leaseUseCases = LeaseUseCasesImpl(repository, FakeHousingRepository())
        val housingUseCases = buildHousingUseCases()
        val syncRequester = RecordingSyncRequester()
        val viewModel = LeaseDetailViewModel(
            ACTIVE_LEASE_ID,
            bailUseCases,
            leaseUseCases,
            housingUseCases,
            tenantUseCases,
            syncRequester
        )

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
            assertEquals(listOf("key_add"), syncRequester.reasons)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteKeyEventUpdatesKeysList() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val bailUseCases = BailUseCasesImpl(repository)
        val leaseUseCases = LeaseUseCasesImpl(repository, FakeHousingRepository())
        val housingUseCases = buildHousingUseCases()
        val viewModel = LeaseDetailViewModel(
            ACTIVE_LEASE_ID,
            bailUseCases,
            leaseUseCases,
            housingUseCases,
            tenantUseCases,
            RecordingSyncRequester()
        )

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

            viewModel.onEvent(LeaseDetailUiEvent.DeleteKeyClicked(DEFAULT_KEYS.first().id))
            advanceUntilIdle()

            val updated = awaitItem()
            assertEquals(1, updated.keys.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun closeLeaseEventMarksLeaseInactive() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val bailUseCases = BailUseCasesImpl(repository)
        val leaseUseCases = LeaseUseCasesImpl(repository, FakeHousingRepository())
        val housingUseCases = buildHousingUseCases()
        val syncRequester = RecordingSyncRequester()
        val viewModel = LeaseDetailViewModel(
            ACTIVE_LEASE_ID,
            bailUseCases,
            leaseUseCases,
            housingUseCases,
            tenantUseCases,
            syncRequester
        )

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
            assertEquals(listOf("lease_close"), syncRequester.reasons)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun missingLeaseIdShowsEmptyStateWithoutCrash() = runTest {
        val repository = FakeLeaseRepository.seeded()
        val bailUseCases = BailUseCasesImpl(repository)
        val leaseUseCases = LeaseUseCasesImpl(repository, FakeHousingRepository())
        val housingUseCases = buildHousingUseCases()
        val viewModel = LeaseDetailViewModel(
            999L,
            bailUseCases,
            leaseUseCases,
            housingUseCases,
            tenantUseCases,
            RecordingSyncRequester()
        )

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
            FakeBailUseCases(
                flow {
                    throw IllegalStateException("boom")
                }
            ),
            FakeLeaseUseCases(),
            FakeHousingUseCases(),
            tenantUseCases,
            RecordingSyncRequester()
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("boom", state.errorMessage)
    }

    private class FakeBailUseCases(
        private val leaseFlow: Flow<Lease?>
    ) : BailUseCases {
        override fun observeBails(): Flow<List<Lease>> = flowOf(emptyList())

        override fun observeBail(leaseId: Long): Flow<Lease?> = leaseFlow

        override fun observeIndexationEvents(leaseId: Long): Flow<List<com.florent.location.domain.model.IndexationEvent>> =
            flowOf(emptyList())

        override fun buildIndexationPolicy(
            bail: Lease,
            todayEpochDay: Long
        ): com.florent.location.domain.model.IndexationPolicy {
            return com.florent.location.domain.model.IndexationPolicy(
                anniversaryEpochDay = bail.startDateEpochDay,
                nextIndexationEpochDay = bail.startDateEpochDay
            )
        }

        override suspend fun simulateIndexationForBail(
            leaseId: Long,
            indexPercent: Double,
            effectiveEpochDay: Long
        ): com.florent.location.domain.model.IndexationSimulation {
            return com.florent.location.domain.model.IndexationSimulation(
                leaseId = leaseId,
                baseRentCents = 0L,
                indexPercent = indexPercent,
                newRentCents = 0L,
                effectiveEpochDay = effectiveEpochDay
            )
        }

        override suspend fun applyIndexationToBail(
            leaseId: Long,
            indexPercent: Double,
            effectiveEpochDay: Long
        ): com.florent.location.domain.model.IndexationEvent {
            return com.florent.location.domain.model.IndexationEvent(
                leaseId = leaseId,
                appliedEpochDay = effectiveEpochDay,
                baseRentCents = 0L,
                indexPercent = indexPercent,
                newRentCents = 0L
            )
        }
    }

    private class FakeLeaseUseCases : LeaseUseCases {
        override suspend fun createLease(request: com.florent.location.domain.usecase.lease.LeaseCreateRequest): Long = 0L

        override fun observeLease(leaseId: Long): Flow<Lease?> = flowOf(null)

        override suspend fun closeLease(leaseId: Long, endEpochDay: Long) = Unit
    }

    private class FakeHousingUseCases : HousingUseCases {
        override fun observeHousings(): Flow<List<Housing>> = flowOf(emptyList())

        override fun observeHousing(id: Long): Flow<Housing?> = flowOf(null)

        override suspend fun createHousing(housing: Housing): Long = 0L

        override suspend fun updateHousing(housing: Housing) = Unit

        override suspend fun deleteHousing(id: Long) = Unit

        override fun observeKeysForHousing(housingId: Long): Flow<List<Key>> = flowOf(emptyList())

        override suspend fun addKey(housingId: Long, key: Key): Long = 0L

        override suspend fun deleteKey(keyId: Long) = Unit
    }

    private fun buildHousingUseCases(keys: List<Key> = DEFAULT_KEYS): HousingUseCases {
        val housing = sampleHousing(id = ACTIVE_HOUSING_ID, city = "Bruxelles")
        val repository = FakeHousingRepository(initialHousings = listOf(housing), initialKeys = keys)
        return HousingUseCasesImpl(repository)
    }

    private fun epochDayToDate(epochDay: Long): String {
        return LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private companion object {
        val DEFAULT_KEYS = listOf(
            Key(
                id = 101L,
                housingId = ACTIVE_HOUSING_ID,
                type = "Clé",
                deviceLabel = "Entrée",
                handedOverEpochDay = START_EPOCH_DAY
            ),
            Key(
                id = 102L,
                housingId = ACTIVE_HOUSING_ID,
                type = "Badge",
                deviceLabel = "Garage",
                handedOverEpochDay = START_EPOCH_DAY
            )
        )
    }
}
