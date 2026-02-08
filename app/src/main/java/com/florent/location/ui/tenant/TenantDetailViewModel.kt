package com.florent.location.ui.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.model.TenantSituation
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.domain.usecase.tenant.ObserveTenantSituation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TenantDetailUiState(
    val isLoading: Boolean = true,
    val tenant: Tenant? = null,
    val situation: TenantSituation? = null,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
    val isDeleted: Boolean = false,
    val isEditRequested: Boolean = false
)

sealed interface TenantDetailUiEvent {
    data object Delete : TenantDetailUiEvent
    data object Edit : TenantDetailUiEvent
}

class TenantDetailViewModel(
    private val tenantId: Long,
    private val tenantUseCases: TenantUseCases,
    private val observeTenantSituation: ObserveTenantSituation
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantDetailUiState())
    val uiState: StateFlow<TenantDetailUiState> = _uiState

    init {
        observeTenant()
    }

    fun onEvent(event: TenantDetailUiEvent) {
        when (event) {
            TenantDetailUiEvent.Delete -> deleteTenant()
            TenantDetailUiEvent.Edit -> _uiState.update { it.copy(isEditRequested = true) }
        }
    }

    private fun observeTenant() {
        viewModelScope.launch {
            tenantUseCases.observeTenant(tenantId)
                .flatMapLatest { tenant ->
                    if (tenant == null) {
                        flowOf(Pair<Tenant?, TenantSituation?>(null, null))
                    } else {
                        observeTenantSituation(tenant)
                            .map { situation -> tenant to situation }
                    }
                }
                .onStart { _uiState.update { it.copy(isLoading = true, errorMessage = null) } }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement du locataire."
                        )
                    }
                }
                .collect { (tenant, situation) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tenant = tenant,
                            situation = situation,
                            isEmpty = tenant == null,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun deleteTenant() {
        viewModelScope.launch {
            try {
                tenantUseCases.deleteTenant(tenantId)
                _uiState.update { it.copy(isDeleted = true, errorMessage = null) }
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }
}
