package com.florent.location.ui.tenant

import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.tenant.TenantUseCasesImpl
import com.florent.location.fake.FakeTenantRepository
import com.florent.location.testutils.RecordingSyncRequester
import com.florent.location.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TenantEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `create new tenant updates fields and saves`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)
        val syncRequester = RecordingSyncRequester()
        val viewModel = TenantEditViewModel(
            tenantId = null,
            useCases = useCases,
            syncManager = syncRequester
        )

        viewModel.onEvent(TenantEditUiEvent.FieldChanged(TenantField.FirstName, "Alice"))
        viewModel.onEvent(TenantEditUiEvent.FieldChanged(TenantField.LastName, "Durand"))
        viewModel.onEvent(TenantEditUiEvent.FieldChanged(TenantField.Phone, "123"))
        viewModel.onEvent(TenantEditUiEvent.FieldChanged(TenantField.Email, "alice@example.com"))

        viewModel.onEvent(TenantEditUiEvent.SaveClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        val tenants = repository.observeTenants().first()
        assertEquals(1, tenants.size)
        assertEquals(listOf("tenant_create"), syncRequester.reasons)
    }

    @Test
    fun `edit existing tenant loads data and saves update`() = runTest {
        val repository = FakeTenantRepository(
            listOf(Tenant(1L, "Alice", "Durand", "123", null))
        )
        val useCases = TenantUseCasesImpl(repository)
        val syncRequester = RecordingSyncRequester()
        val viewModel = TenantEditViewModel(
            tenantId = 1L,
            useCases = useCases,
            syncManager = syncRequester
        )

        advanceUntilIdle()
        assertEquals("Alice", viewModel.uiState.value.firstName)

        viewModel.onEvent(TenantEditUiEvent.FieldChanged(TenantField.FirstName, "Alicia"))
        viewModel.onEvent(TenantEditUiEvent.SaveClicked)
        advanceUntilIdle()

        val updated = repository.observeTenant(1L).first()
        assertEquals("Alicia", updated?.firstName)
        assertTrue(viewModel.uiState.value.isSaved)
        assertEquals(listOf("tenant_update"), syncRequester.reasons)
    }

    @Test
    fun `validation errors surface in ui state`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)
        val syncRequester = RecordingSyncRequester()
        val viewModel = TenantEditViewModel(
            tenantId = null,
            useCases = useCases,
            syncManager = syncRequester
        )

        viewModel.onEvent(TenantEditUiEvent.FieldChanged(TenantField.FirstName, ""))
        viewModel.onEvent(TenantEditUiEvent.FieldChanged(TenantField.LastName, ""))
        viewModel.onEvent(TenantEditUiEvent.SaveClicked)
        advanceUntilIdle()

        assertEquals("Le prénom est obligatoire.", viewModel.uiState.value.errorMessage)
    }
}
