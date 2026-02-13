package com.florent.location.ui.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.model.TenantStatus
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.presentation.sync.HousingSyncRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TenantEditUiState(
    val isLoading: Boolean = false,
    val tenantId: Long? = null,
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val status: TenantStatus = TenantStatus.ACTIVE,
    val statusDropdownExpanded: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)

enum class TenantField {
    FirstName,
    LastName,
    Phone,
    Email
}

sealed interface TenantEditUiEvent {
    data class FieldChanged(val field: TenantField, val value: String) : TenantEditUiEvent
    data class StatusChanged(val status: TenantStatus) : TenantEditUiEvent
    data class StatusDropdownExpanded(val expanded: Boolean) : TenantEditUiEvent
    data object SaveClicked : TenantEditUiEvent
}

class TenantEditViewModel(
    private val tenantId: Long?,
    private val useCases: TenantUseCases,
    private val syncManager: HousingSyncRequester
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantEditUiState(isLoading = tenantId != null, tenantId = tenantId))
    val uiState: StateFlow<TenantEditUiState> = _uiState

    init {
        if (tenantId != null) {
            observeTenant(tenantId)
        }
    }

    fun onEvent(event: TenantEditUiEvent) {
        when (event) {
            is TenantEditUiEvent.FieldChanged -> updateField(event.field, event.value)
            is TenantEditUiEvent.StatusChanged -> updateStatus(event.status)
            is TenantEditUiEvent.StatusDropdownExpanded ->
                _uiState.update { it.copy(statusDropdownExpanded = event.expanded) }
            TenantEditUiEvent.SaveClicked -> saveTenant()
        }
    }

    private fun observeTenant(id: Long) {
        viewModelScope.launch {
            useCases.observeTenant(id)
                .onStart { _uiState.update { it.copy(isLoading = true, errorMessage = null) } }
                .catch { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
                .collect { tenant ->
                    if (tenant == null) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Locataire introuvable.") }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                tenantId = tenant.id,
                                firstName = tenant.firstName,
                                lastName = tenant.lastName,
                                phone = tenant.phone.orEmpty(),
                                email = tenant.email.orEmpty(),
                                status = tenant.status,
                                errorMessage = null
                            )
                        }
                    }
                }
        }
    }

    private fun updateField(field: TenantField, value: String) {
        _uiState.update {
            when (field) {
                TenantField.FirstName -> it.copy(firstName = value, errorMessage = null)
                TenantField.LastName -> it.copy(lastName = value, errorMessage = null)
                TenantField.Phone -> it.copy(phone = value, errorMessage = null)
                TenantField.Email -> it.copy(email = value, errorMessage = null)
            }
        }
    }

    private fun saveTenant() {
        viewModelScope.launch {
            val current = _uiState.value
            val tenant = Tenant(
                id = current.tenantId ?: 0L,
                firstName = current.firstName.trim(),
                lastName = current.lastName.trim(),
                phone = current.phone.trim().ifBlank { null },
                email = current.email.trim().ifBlank { null },
                status = current.status
            )
            try {
                if (current.tenantId == null) {
                    useCases.createTenant(tenant)
                    syncManager.requestSync("tenant_create")
                } else {
                    useCases.updateTenant(tenant)
                    syncManager.requestSync("tenant_update")
                }
                _uiState.update { it.copy(isSaved = true, errorMessage = null) }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message, isSaved = false) }
            }
        }
    }

    private fun updateStatus(status: TenantStatus) {
        _uiState.update { it.copy(status = status, errorMessage = null) }
    }
}
