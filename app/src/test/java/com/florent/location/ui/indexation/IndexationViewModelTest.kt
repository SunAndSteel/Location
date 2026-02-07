package com.florent.location.ui.indexation

import com.florent.location.domain.model.UpcomingIndexation
import com.florent.location.domain.usecase.indexation.IndexationUseCases
import com.florent.location.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IndexationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `emits empty state when there are no active leases`() = runTest {
        val useCases = FakeIndexationUseCases(flowOf(emptyList()))

        val viewModel = IndexationViewModel(useCases)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertTrue(state.upcomingIndexations.isEmpty())
        assertEquals(false, state.isLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `emits list state with sorted order`() = runTest {
        val indexations = listOf(
            UpcomingIndexation(
                leaseId = 2L,
                housingId = 12L,
                tenantId = 22L,
                nextIndexationEpochDay = 20010L,
                daysUntil = 20
            ),
            UpcomingIndexation(
                leaseId = 1L,
                housingId = 11L,
                tenantId = 21L,
                nextIndexationEpochDay = 20005L,
                daysUntil = 10
            )
        ).sortedBy { it.daysUntil }
        val useCases = FakeIndexationUseCases(flowOf(indexations))

        val viewModel = IndexationViewModel(useCases)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1L, 2L), state.upcomingIndexations.map { it.leaseId })
        assertTrue(state.upcomingIndexations.zipWithNext().all { (a, b) -> a.daysUntil <= b.daysUntil })
        assertEquals(false, state.isLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `maps errors to ui state without crashing`() = runTest {
        val useCases = FakeIndexationUseCases(
            flow {
                throw IllegalStateException("boom")
            }
        )

        val viewModel = IndexationViewModel(useCases)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals("boom", state.errorMessage)
    }

    private class FakeIndexationUseCases(
        private val flow: Flow<List<UpcomingIndexation>>
    ) : IndexationUseCases {
        override fun observeUpcomingIndexations(todayEpochDay: Long): Flow<List<UpcomingIndexation>> =
            flow
    }
}
