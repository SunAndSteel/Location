package com.florent.location.ui.lease

import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.housing.HousingUseCasesImpl
import com.florent.location.domain.usecase.lease.LeaseCreateRequest
import com.florent.location.domain.usecase.lease.LeaseUseCasesImpl
import com.florent.location.domain.usecase.tenant.TenantUseCasesImpl
import com.florent.location.fake.FakeHousingRepository
import com.florent.location.fake.FakeLeaseRepository
import com.florent.location.fake.FakeTenantRepository
import com.florent.location.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaseCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial UiState has empty fields and is not saving`() = runTest {
        val housingRepository = FakeHousingRepository()
        val tenantRepository = FakeTenantRepository()
        val leaseRepository = FakeLeaseRepository()
        val viewModel = LeaseCreateViewModel(
            housingUseCases = HousingUseCasesImpl(housingRepository),
            tenantUseCases = TenantUseCasesImpl(tenantRepository),
            leaseUseCases = LeaseUseCasesImpl(leaseRepository, housingRepository)
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
        assertEquals("", state.startDate)
        assertEquals("", state.rent)
        assertEquals("", state.charges)
        assertEquals("", state.deposit)
        assertNull(state.selectedHousingId)
        assertNull(state.selectedTenantId)
    }

    @Test
    fun `selecting housing and tenant updates UiState`() = runTest {
        val housingRepository = FakeHousingRepository(
            listOf(Housing(id = 1L, city = "Paris", address = "1 rue A"))
        )
        val tenantRepository = FakeTenantRepository(
            listOf(Tenant(id = 2L, firstName = "Ada", lastName = "Lovelace", phone = null, email = null))
        )
        val leaseRepository = FakeLeaseRepository(existingHousingIds = setOf(1L), existingTenantIds = setOf(2L))
        val viewModel = LeaseCreateViewModel(
            housingUseCases = HousingUseCasesImpl(housingRepository),
            tenantUseCases = TenantUseCasesImpl(tenantRepository),
            leaseUseCases = LeaseUseCasesImpl(leaseRepository, housingRepository)
        )

        advanceUntilIdle()
        viewModel.onEvent(LeaseCreateUiEvent.SelectHousing(1L))
        viewModel.onEvent(LeaseCreateUiEvent.SelectTenant(2L))

        val state = viewModel.uiState.value
        assertEquals(1L, state.selectedHousingId)
        assertEquals(2L, state.selectedTenantId)
    }

    @Test
    fun `save success triggers success flag and resets saving state`() = runTest {
        val housingRepository = FakeHousingRepository(
            listOf(Housing(id = 1L, city = "Paris", address = "1 rue A"))
        )
        val tenantRepository = FakeTenantRepository(
            listOf(Tenant(id = 2L, firstName = "Ada", lastName = "Lovelace", phone = null, email = null))
        )
        val leaseRepository = FakeLeaseRepository(existingHousingIds = setOf(1L), existingTenantIds = setOf(2L))
        val viewModel = LeaseCreateViewModel(
            housingUseCases = HousingUseCasesImpl(housingRepository),
            tenantUseCases = TenantUseCasesImpl(tenantRepository),
            leaseUseCases = LeaseUseCasesImpl(leaseRepository, housingRepository)
        )

        advanceUntilIdle()
        viewModel.onEvent(LeaseCreateUiEvent.SelectHousing(1L))
        viewModel.onEvent(LeaseCreateUiEvent.SelectTenant(2L))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.StartDate, epochDayToDate(1000)))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Rent, "1000,00"))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Charges, "100,00"))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Deposit, "500,00"))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.RentDueDay, "5"))

        viewModel.onEvent(LeaseCreateUiEvent.SaveClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertFalse(state.isSaving)
        assertNull(state.errorMessage)
    }

    @Test
    fun `save failure when active lease exists sets error state`() = runTest {
        val housingRepository = FakeHousingRepository(
            listOf(Housing(id = 1L, city = "Paris", address = "1 rue A"))
        )
        val tenantRepository = FakeTenantRepository(
            listOf(Tenant(id = 2L, firstName = "Ada", lastName = "Lovelace", phone = null, email = null))
        )
        val leaseRepository = FakeLeaseRepository(existingHousingIds = setOf(1L), existingTenantIds = setOf(2L))
        val leaseUseCases = LeaseUseCasesImpl(leaseRepository, housingRepository)
        leaseUseCases.createLease(
            LeaseCreateRequest(
                housingId = 1L,
                tenantId = 2L,
                startDateEpochDay = 900L,
                rentCents = 100000L,
                chargesCents = 10000L,
                depositCents = 50000L,
                rentDueDayOfMonth = 5
            )
        )

        val viewModel = LeaseCreateViewModel(
            housingUseCases = HousingUseCasesImpl(housingRepository),
            tenantUseCases = TenantUseCasesImpl(tenantRepository),
            leaseUseCases = leaseUseCases
        )

        advanceUntilIdle()
        viewModel.onEvent(LeaseCreateUiEvent.SelectHousing(1L))
        viewModel.onEvent(LeaseCreateUiEvent.SelectTenant(2L))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.StartDate, epochDayToDate(1000)))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Rent, "1000,00"))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Charges, "100,00"))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Deposit, "500,00"))
        viewModel.onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.RentDueDay, "5"))

        viewModel.onEvent(LeaseCreateUiEvent.SaveClicked)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSaving)
        assertEquals("Un bail actif existe déjà pour ce logement.", state.errorMessage)
    }

    private fun epochDayToDate(epochDay: Long): String {
        return LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
