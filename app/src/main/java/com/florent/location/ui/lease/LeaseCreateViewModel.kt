package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.lease.LeaseCreateRequest
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.ui.components.formatEuroInput
import com.florent.location.ui.components.parseEuroInputToCents
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaseCreateUiState(
    val isLoading: Boolean = true,
    val housings: List<Housing> = emptyList(),
    val tenants: List<Tenant> = emptyList(),
    val selectedHousingId: Long? = null,
    val selectedTenantId: Long? = null,
    val startDate: String = "",
    val rent: String = "",
    val charges: String = "",
    val deposit: String = "",
    val housingDefaultRentCents: Long = 0L,
    val housingDefaultChargesCents: Long = 0L,
    val housingDepositCents: Long = 0L,
    val rentOverridden: Boolean = false,
    val chargesOverridden: Boolean = false,
    val depositOverridden: Boolean = false,
    val rentDueDayOfMonth: String = "1",
    val housingDropdownExpanded: Boolean = false,
    val tenantDropdownExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val savedLeaseId: Long? = null,
    val errorMessage: String? = null
)

enum class LeaseField {
    StartDate,
    Rent,
    Charges,
    Deposit,
    RentDueDay
}

sealed interface LeaseCreateUiEvent {
    data class SelectHousing(val housingId: Long) : LeaseCreateUiEvent
    data class SelectTenant(val tenantId: Long) : LeaseCreateUiEvent
    data class FieldChanged(val field: LeaseField, val value: String) : LeaseCreateUiEvent
    data class HousingDropdownExpanded(val expanded: Boolean) : LeaseCreateUiEvent
    data class TenantDropdownExpanded(val expanded: Boolean) : LeaseCreateUiEvent
    data object SaveClicked : LeaseCreateUiEvent
}

class LeaseCreateViewModel(
    private val housingUseCases: HousingUseCases,
    private val tenantUseCases: TenantUseCases,
    private val leaseUseCases: LeaseUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaseCreateUiState())
    val uiState: StateFlow<LeaseCreateUiState> = _uiState

    init {
        observeFormData()
    }

    fun onEvent(event: LeaseCreateUiEvent) {
        when (event) {
            is LeaseCreateUiEvent.SelectHousing -> selectHousing(event.housingId)
            is LeaseCreateUiEvent.SelectTenant -> selectTenant(event.tenantId)
            is LeaseCreateUiEvent.FieldChanged -> updateField(event.field, event.value)
            is LeaseCreateUiEvent.HousingDropdownExpanded ->
                _uiState.update { it.copy(housingDropdownExpanded = event.expanded) }
            is LeaseCreateUiEvent.TenantDropdownExpanded ->
                _uiState.update { it.copy(tenantDropdownExpanded = event.expanded) }
            LeaseCreateUiEvent.SaveClicked -> saveLease()
        }
    }

    private fun observeFormData() {
        viewModelScope.launch {
            combine(
                housingUseCases.observeHousings(),
                tenantUseCases.observeTenants()
            ) { housings, tenants ->
                housings to tenants
            }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement des données."
                        )
                    }
                }
                .collect { (housings, tenants) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            housings = housings,
                            tenants = tenants,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun selectHousing(housingId: Long) {
        val housing = _uiState.value.housings.firstOrNull { it.id == housingId }
        _uiState.update { current ->
            val defaultRent = housing?.defaultRentCents ?: current.housingDefaultRentCents
            val defaultCharges = housing?.defaultChargesCents ?: current.housingDefaultChargesCents
            val defaultDeposit = housing?.depositCents ?: current.housingDepositCents
            current.copy(
                selectedHousingId = housingId,
                housingDropdownExpanded = false,
                rent = housing?.let { formatEuroInput(defaultRent) } ?: current.rent,
                charges = housing?.let { formatEuroInput(defaultCharges) } ?: current.charges,
                deposit = housing?.let { formatEuroInput(defaultDeposit) } ?: current.deposit,
                housingDefaultRentCents = defaultRent,
                housingDefaultChargesCents = defaultCharges,
                housingDepositCents = defaultDeposit,
                rentOverridden = false,
                chargesOverridden = false,
                depositOverridden = false,
                errorMessage = null
            )
        }
    }

    private fun selectTenant(tenantId: Long) {
        _uiState.update {
            it.copy(
                selectedTenantId = tenantId,
                tenantDropdownExpanded = false,
                errorMessage = null
            )
        }
    }

    private fun updateField(field: LeaseField, value: String) {
        _uiState.update {
            when (field) {
                LeaseField.StartDate -> it.copy(startDate = value, errorMessage = null)
                LeaseField.Rent -> {
                    val rentOverridden = isOverridden(value, it.housingDefaultRentCents)
                    it.copy(rent = value, rentOverridden = rentOverridden, errorMessage = null)
                }
                LeaseField.Charges -> {
                    val chargesOverridden = isOverridden(value, it.housingDefaultChargesCents)
                    it.copy(charges = value, chargesOverridden = chargesOverridden, errorMessage = null)
                }
                LeaseField.Deposit -> {
                    val depositOverridden = isOverridden(value, it.housingDepositCents)
                    it.copy(deposit = value, depositOverridden = depositOverridden, errorMessage = null)
                }
                LeaseField.RentDueDay -> it.copy(rentDueDayOfMonth = value, errorMessage = null)
            }
        }
    }

    private fun saveLease() {
        viewModelScope.launch {
            val current = _uiState.value
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    isSaved = false,
                    savedLeaseId = null
                )
            }

            val startDateEpochDay = parseEpochDay(current.startDate)
            val rentCents = parseEuroInputToCents(current.rent)
            val chargesCents = parseEuroInputToCents(current.charges)
            val depositCents = parseEuroInputToCents(current.deposit)
            val rentDueDay = current.rentDueDayOfMonth.toIntOrNull() ?: 0

            val request = LeaseCreateRequest(
                housingId = current.selectedHousingId,
                tenantId = current.selectedTenantId,
                startDateEpochDay = startDateEpochDay,
                rentCents = rentCents,
                chargesCents = chargesCents,
                depositCents = depositCents,
                rentDueDayOfMonth = rentDueDay
            )

            try {
                val leaseId = leaseUseCases.createLease(request)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true,
                        savedLeaseId = leaseId,
                        errorMessage = null
                    )
                }
            } catch (error: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message,
                        isSaved = false,
                        savedLeaseId = null
                    )
                }
            }
        }
    }

    private fun parseEpochDay(value: String): Long? {
        if (value.isBlank()) return null
        return runCatching {
            LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE).toEpochDay()
        }.getOrNull()
    }

    private fun isOverridden(value: String, housingDefaultCents: Long): Boolean {
        val parsed = parseEuroInputToCents(value) ?: return false
        return parsed != housingDefaultCents
    }
}
