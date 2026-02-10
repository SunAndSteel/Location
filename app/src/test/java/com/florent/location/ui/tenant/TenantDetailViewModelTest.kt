package com.florent.location.ui.tenant

import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.TenantUseCasesImpl
import com.florent.location.domain.usecase.tenant.ObserveTenantSituation
import com.florent.location.fake.FakeLeaseRepository
import com.florent.location.fake.FakeTenantRepository
import com.florent.location.testutils.RecordingSyncRequester
import com.florent.location.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TenantDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `observes tenant and updates when data changes`() = runTest {
        val repository = FakeTenantRepository(
            listOf(Tenant(1L, "Alice", "Durand", null, null))
        )
        val useCases = TenantUseCasesImpl(repository)
        val observeTenantSituation = ObserveTenantSituation(FakeLeaseRepository())
        val viewModel = TenantDetailViewModel(
            tenantId = 1L,
            tenantUseCases = useCases,
            observeTenantSituation = observeTenantSituation,
            syncManager = RecordingSyncRequester()
        )

        advanceUntilIdle()
        assertEquals("Alice", viewModel.uiState.value.tenant?.firstName)

        repository.setTenants(listOf(Tenant(1L, "Alicia", "Durand", null, null)))
        advanceUntilIdle()
        assertEquals("Alicia", viewModel.uiState.value.tenant?.firstName)
    }

    @Test
    fun `delete event marks tenant as deleted and requests sync`() = runTest {
        val repository = FakeTenantRepository(
            listOf(Tenant(1L, "Alice", "Durand", null, null))
        )
        val useCases = TenantUseCasesImpl(repository)
        val syncRequester = RecordingSyncRequester()
        val viewModel = TenantDetailViewModel(
            tenantId = 1L,
            tenantUseCases = useCases,
            observeTenantSituation = ObserveTenantSituation(FakeLeaseRepository()),
            syncManager = syncRequester
        )

        advanceUntilIdle()
        viewModel.onEvent(TenantDetailUiEvent.Delete)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isDeleted)
        assertEquals(listOf("tenant_delete"), syncRequester.reasons)
    }

    @Test
    fun `missing tenant leads to empty state`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)
        val observeTenantSituation = ObserveTenantSituation(FakeLeaseRepository())
        val viewModel = TenantDetailViewModel(
            tenantId = 99L,
            tenantUseCases = useCases,
            observeTenantSituation = observeTenantSituation,
            syncManager = RecordingSyncRequester()
        )

        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isEmpty)
    }
}
