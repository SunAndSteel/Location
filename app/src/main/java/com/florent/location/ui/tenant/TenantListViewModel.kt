package com.florent.location.ui.tenant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.model.TenantSituation
import com.florent.location.domain.usecase.tenant.TenantUseCases
import com.florent.location.presentation.sync.HousingSyncRequester
import com.florent.location.domain.usecase.tenant.ObserveTenantSituation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TenantListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val tenants: List<TenantListItem> = emptyList(),
    val isEmpty: Boolean = false,
    val errorMessage: String? = null,
    val selectedTenantId: Long? = null
)

data class TenantListItem(
    val tenant: Tenant,
    val situation: TenantSituation
)

sealed interface TenantListUiEvent {
    data class SearchQueryChanged(val value: String) : TenantListUiEvent
    data class DeleteTenantClicked(val id: Long) : TenantListUiEvent
    data class TenantClicked(val id: Long) : TenantListUiEvent
}

class TenantListViewModel(
    private val useCases: TenantUseCases,
    private val observeTenantSituation: ObserveTenantSituation,
    private val syncManager: HousingSyncRequester
) : ViewModel() {

    private val _uiState = MutableStateFlow(TenantListUiState())
    val uiState: StateFlow<TenantListUiState> = _uiState

    private var allTenants: List<TenantListItem> = emptyList()

    init {
        observeTenants()
    }

    fun onEvent(event: TenantListUiEvent) {
        when (event) {
            is TenantListUiEvent.SearchQueryChanged -> updateSearch(event.value)
            is TenantListUiEvent.DeleteTenantClicked -> deleteTenant(event.id)
            is TenantListUiEvent.TenantClicked -> selectTenant(event.id)
        }
    }

    private fun observeTenants() {
        viewModelScope.launch {
            useCases.observeTenants()
                .flatMapLatest { tenants ->
                    if (tenants.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        combine(
                            tenants.map { tenant ->
                                observeTenantSituation(tenant)
                                    .map { situation -> TenantListItem(tenant, situation) }
                            }
                        ) { items -> items.toList() }
                    }
                }
                .onStart { _uiState.update { it.copy(isLoading = true, errorMessage = null) } }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement des locataires."
                        )
                    }
                }
                .collect { tenants ->
                    allTenants = tenants
                    val filtered = applySearch(_uiState.value.searchQuery, tenants)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tenants = filtered,
                            isEmpty = filtered.isEmpty(),
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun updateSearch(query: String) {
        val filtered = applySearch(query, allTenants)
        _uiState.update {
            it.copy(
                searchQuery = query,
                tenants = filtered,
                isEmpty = filtered.isEmpty(),
                errorMessage = null
            )
        }
    }

    private fun selectTenant(id: Long) {
        _uiState.update { it.copy(selectedTenantId = id) }
    }

    private fun deleteTenant(id: Long) {
        viewModelScope.launch {
            try {
                useCases.deleteTenant(id)
                syncManager.requestSync("tenant_delete")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun applySearch(query: String, tenants: List<TenantListItem>): List<TenantListItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return tenants
        return tenants.filter { item ->
            val tenant = item.tenant
            tenant.firstName.contains(trimmed, ignoreCase = true) ||
                tenant.lastName.contains(trimmed, ignoreCase = true) ||
                tenant.email?.contains(trimmed, ignoreCase = true) == true ||
                tenant.phone?.contains(trimmed, ignoreCase = true) == true
        }
    }
}
