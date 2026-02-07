package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Key
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.lease.LeaseCreateRequest
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class KeyDraft(
    val type: String = "",
    val deviceLabel: String = "",
    val handedOverEpochDay: String = ""
)

data class LeaseCreateUiState(
    val isLoading: Boolean = true,
    val housings: List<Housing> = emptyList(),
    val tenants: List<Tenant> = emptyList(),
    val selectedHousingId: Long? = null,
    val selectedTenantId: Long? = null,
    val startDateEpochDay: String = "",
    val rentCents: String = "",
    val chargesCents: String = "",
    val depositCents: String = "",
    val rentDueDayOfMonth: String = "1",
    val mailboxLabel: String = "",
    val meterGas: String = "",
    val meterElectricity: String = "",
    val meterWater: String = "",
    val keys: List<KeyDraft> = emptyList(),
    val housingDropdownExpanded: Boolean = false,
    val tenantDropdownExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

enum class LeaseField {
    StartDate,
    Rent,
    Charges,
    Deposit,
    RentDueDay,
    MailboxLabel,
    MeterGas,
    MeterElectricity,
    MeterWater
}

enum class KeyField {
    Type,
    DeviceLabel,
    HandedOverEpochDay
}

sealed interface LeaseCreateUiEvent {
    data class SelectHousing(val housingId: Long) : LeaseCreateUiEvent
    data class SelectTenant(val tenantId: Long) : LeaseCreateUiEvent
    data class FieldChanged(val field: LeaseField, val value: String) : LeaseCreateUiEvent
    data class KeyFieldChanged(val index: Int, val field: KeyField, val value: String) : LeaseCreateUiEvent
    data class HousingDropdownExpanded(val expanded: Boolean) : LeaseCreateUiEvent
    data class TenantDropdownExpanded(val expanded: Boolean) : LeaseCreateUiEvent
    data object AddKey : LeaseCreateUiEvent
    data class RemoveKey(val index: Int) : LeaseCreateUiEvent
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
            is LeaseCreateUiEvent.KeyFieldChanged -> updateKeyField(event.index, event.field, event.value)
            is LeaseCreateUiEvent.HousingDropdownExpanded ->
                _uiState.update { it.copy(housingDropdownExpanded = event.expanded) }
            is LeaseCreateUiEvent.TenantDropdownExpanded ->
                _uiState.update { it.copy(tenantDropdownExpanded = event.expanded) }
            LeaseCreateUiEvent.AddKey -> addKey()
            is LeaseCreateUiEvent.RemoveKey -> removeKey(event.index)
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
        _uiState.update {
            it.copy(
                selectedHousingId = housingId,
                housingDropdownExpanded = false,
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
                LeaseField.StartDate -> it.copy(startDateEpochDay = value, errorMessage = null)
                LeaseField.Rent -> it.copy(rentCents = value, errorMessage = null)
                LeaseField.Charges -> it.copy(chargesCents = value, errorMessage = null)
                LeaseField.Deposit -> it.copy(depositCents = value, errorMessage = null)
                LeaseField.RentDueDay -> it.copy(rentDueDayOfMonth = value, errorMessage = null)
                LeaseField.MailboxLabel -> it.copy(mailboxLabel = value)
                LeaseField.MeterGas -> it.copy(meterGas = value)
                LeaseField.MeterElectricity -> it.copy(meterElectricity = value)
                LeaseField.MeterWater -> it.copy(meterWater = value)
            }
        }
    }

    private fun updateKeyField(index: Int, field: KeyField, value: String) {
        _uiState.update { current ->
            val updatedKeys = current.keys.toMutableList()
            if (index in updatedKeys.indices) {
                val key = updatedKeys[index]
                updatedKeys[index] = when (field) {
                    KeyField.Type -> key.copy(type = value)
                    KeyField.DeviceLabel -> key.copy(deviceLabel = value)
                    KeyField.HandedOverEpochDay -> key.copy(handedOverEpochDay = value)
                }
            }
            current.copy(keys = updatedKeys, errorMessage = null)
        }
    }

    private fun addKey() {
        _uiState.update { current ->
            current.copy(keys = current.keys + KeyDraft())
        }
    }

    private fun removeKey(index: Int) {
        _uiState.update { current ->
            if (index !in current.keys.indices) {
                current
            } else {
                val updatedKeys = current.keys.toMutableList()
                updatedKeys.removeAt(index)
                current.copy(keys = updatedKeys)
            }
        }
    }

    private fun saveLease() {
        viewModelScope.launch {
            val current = _uiState.value
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSaved = false) }

            val startDateEpochDay = current.startDateEpochDay.toLongOrNull()
            val rentCents = current.rentCents.toLongOrNull() ?: 0L
            val chargesCents = current.chargesCents.toLongOrNull() ?: 0L
            val depositCents = current.depositCents.toLongOrNull() ?: 0L
            val rentDueDay = current.rentDueDayOfMonth.toIntOrNull() ?: 0

            val keys = current.keys.map { draft ->
                val handedOverEpochDay = draft.handedOverEpochDay.toLongOrNull()
                    ?: startDateEpochDay
                    ?: 0L
                Key(
                    id = 0L,
                    leaseId = 0L,
                    type = draft.type.trim(),
                    deviceLabel = draft.deviceLabel.trim().ifBlank { null },
                    handedOverEpochDay = handedOverEpochDay
                )
            }

            val request = LeaseCreateRequest(
                housingId = current.selectedHousingId,
                tenantId = current.selectedTenantId,
                startDateEpochDay = startDateEpochDay,
                rentCents = rentCents,
                chargesCents = chargesCents,
                depositCents = depositCents,
                rentDueDayOfMonth = rentDueDay,
                mailboxLabel = current.mailboxLabel.trim().ifBlank { null },
                meterGas = current.meterGas.trim().ifBlank { null },
                meterElectricity = current.meterElectricity.trim().ifBlank { null },
                meterWater = current.meterWater.trim().ifBlank { null },
                keys = keys
            )

            try {
                leaseUseCases.createLease(request)
                _uiState.update { it.copy(isSaving = false, isSaved = true, errorMessage = null) }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message, isSaved = false) }
            }
        }
    }
}
