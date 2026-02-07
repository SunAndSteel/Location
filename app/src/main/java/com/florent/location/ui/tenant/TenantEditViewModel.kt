package com.florent.location.ui.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.tenant.TenantUseCases
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
    data object SaveClicked : TenantEditUiEvent
}

class TenantEditViewModel(
    private val tenantId: Long?,
    private val useCases: TenantUseCases
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
                email = current.email.trim().ifBlank { null }
            )
            try {
                if (current.tenantId == null) {
                    useCases.createTenant(tenant)
                } else {
                    useCases.updateTenant(tenant)
                }
                _uiState.update { it.copy(isSaved = true, errorMessage = null) }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message, isSaved = false) }
            }
        }
    }
}
