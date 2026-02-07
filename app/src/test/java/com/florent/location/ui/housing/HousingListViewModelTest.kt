package com.florent.location.ui.housing

import com.florent.location.domain.model.Housing
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.housing.HousingUseCasesImpl
import com.florent.location.fake.FakeHousingRepository
import com.florent.location.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HousingListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state transitions to empty`() = runTest {
        val repository = FakeHousingRepository()
        val useCases = HousingUseCasesImpl(repository)
        val viewModel = HousingListViewModel(useCases)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertEquals(0, state.housings.size)
    }

    @Test
    fun `observe housings failure sets error state`() = runTest {
        val viewModel = HousingListViewModel(
            FakeHousingUseCases(
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

    @Test
    fun `delete housing shows error when active lease exists`() = runTest {
        val housing = Housing(
            id = 1L,
            city = "Bruxelles",
            address = "1 Rue de la Paix",
            defaultRentCents = 90000,
            defaultChargesCents = 15000,
            depositCents = 180000
        )
        val repository = FakeHousingRepository(listOf(housing))
        repository.setActiveLease(housing.id, true)
        val useCases = HousingUseCasesImpl(repository)
        val viewModel = HousingListViewModel(useCases)

        advanceUntilIdle()
        viewModel.onEvent(HousingListUiEvent.DeleteHousing(housing.id))
        advanceUntilIdle()

        assertEquals(
            "Suppression impossible: un bail actif existe pour ce logement.",
            viewModel.uiState.value.errorMessage
        )
    }

    private class FakeHousingUseCases(
        private val housingsFlow: Flow<List<Housing>>
    ) : HousingUseCases {
        override fun observeHousings(): Flow<List<Housing>> = housingsFlow

        override fun observeHousing(id: Long): Flow<Housing?> = flowOf(null)

        override suspend fun createHousing(housing: Housing): Long = 0L

        override suspend fun updateHousing(housing: Housing) = Unit

        override suspend fun deleteHousing(id: Long) = Unit
    }
}
