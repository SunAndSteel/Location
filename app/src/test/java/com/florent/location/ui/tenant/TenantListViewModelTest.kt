package com.florent.location.ui.tenant

import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.TenantUseCasesImpl
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.domain.usecase.tenant.ObserveTenantSituation
import com.florent.location.fake.FakeLeaseRepository
import com.florent.location.fake.FakeTenantRepository
import com.florent.location.util.MainDispatcherRule
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
class TenantListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state transitions to empty`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)
        val viewModel = TenantListViewModel(
            useCases = useCases,
            observeTenantSituation = ObserveTenantSituation(FakeLeaseRepository())
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isEmpty)
        assertEquals(0, state.tenants.size)
    }

    @Test
    fun `search filters by name email or phone`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)
        val tenants = listOf(
            Tenant(1L, "Alice", "Durand", "123", "alice@example.com"),
            Tenant(2L, "Bob", "Martin", "456", "bob@example.com"),
            Tenant(3L, "Claire", "Dupont", null, "claire@example.com")
        )
        repository.setTenants(tenants)
        val viewModel = TenantListViewModel(
            useCases = useCases,
            observeTenantSituation = ObserveTenantSituation(FakeLeaseRepository())
        )

        advanceUntilIdle()
        viewModel.onEvent(TenantListUiEvent.SearchQueryChanged("mar"))
        advanceUntilIdle()
        assertEquals(listOf(tenants[1]), viewModel.uiState.value.tenants.map { it.tenant })

        viewModel.onEvent(TenantListUiEvent.SearchQueryChanged("ALICE"))
        advanceUntilIdle()
        assertEquals(listOf(tenants[0]), viewModel.uiState.value.tenants.map { it.tenant })

        viewModel.onEvent(TenantListUiEvent.SearchQueryChanged("example.com"))
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.tenants.size)

        viewModel.onEvent(TenantListUiEvent.SearchQueryChanged("123"))
        advanceUntilIdle()
        assertEquals(listOf(tenants[0]), viewModel.uiState.value.tenants.map { it.tenant })
    }

    @Test
    fun `delete removes tenant or sets error when forbidden`() = runTest {
        val repository = FakeTenantRepository(
            listOf(
                Tenant(1L, "Alice", "Durand", null, null),
                Tenant(2L, "Bob", "Martin", null, null)
            )
        )
        val useCases = TenantUseCasesImpl(repository)
        val viewModel = TenantListViewModel(
            useCases = useCases,
            observeTenantSituation = ObserveTenantSituation(FakeLeaseRepository())
        )

        advanceUntilIdle()
        viewModel.onEvent(TenantListUiEvent.DeleteTenantClicked(1L))
        advanceUntilIdle()
        assertEquals(
            listOf(Tenant(2L, "Bob", "Martin", null, null)),
            viewModel.uiState.value.tenants.map { it.tenant }
        )

        repository.setActiveLease(2L, true)
        viewModel.onEvent(TenantListUiEvent.DeleteTenantClicked(2L))
        advanceUntilIdle()
        assertEquals(
            "Impossible de supprimer un locataire avec un bail actif.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun `observe tenants failure sets error state`() = runTest {
        val viewModel = TenantListViewModel(
            FakeTenantUseCases(
                flow {
                    throw IllegalStateException("boom")
                }
            ),
            observeTenantSituation = ObserveTenantSituation(FakeLeaseRepository())
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("boom", state.errorMessage)
    }

    private class FakeTenantUseCases(
        private val tenantsFlow: Flow<List<Tenant>>
    ) : TenantUseCases {
        override fun observeTenants(): Flow<List<Tenant>> = tenantsFlow

        override fun observeTenant(id: Long): Flow<Tenant?> = flowOf(null)

        override suspend fun createTenant(tenant: Tenant): Long = 0L

        override suspend fun updateTenant(tenant: Tenant) = Unit

        override suspend fun deleteTenant(id: Long) = Unit
    }
}
