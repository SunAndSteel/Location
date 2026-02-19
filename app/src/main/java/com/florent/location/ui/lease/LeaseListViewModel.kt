package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Lease
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.lease.LeaseUseCases
import com.florent.location.domain.usecase.tenant.TenantUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaseListItem(
    val lease: Lease,
    val tenantName: String,
    val housingLabel: String
)

data class LeaseListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val leases: List<LeaseListItem> = emptyList(),
    val isEmpty: Boolean = false,
    val isSearchResultEmpty: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LeaseListUiEvent {
    data class SearchQueryChanged(val value: String) : LeaseListUiEvent
}

class LeaseListViewModel(
    private val useCases: LeaseUseCases,
    private val housingUseCases: HousingUseCases,
    private val tenantUseCases: TenantUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaseListUiState())
    val uiState: StateFlow<LeaseListUiState> = _uiState

    private var allLeases: List<LeaseListItem> = emptyList()

    init {
        observeLeases()
    }

    fun onEvent(event: LeaseListUiEvent) {
        when (event) {
            is LeaseListUiEvent.SearchQueryChanged -> updateSearch(event.value)
        }
    }

    private fun observeLeases() {
        viewModelScope.launch {
            useCases.observeLeases()
                .combine(housingUseCases.observeHousings()) { leases, housings ->
                    leases to housings.associateBy(Housing::id)
                }
                .combine(tenantUseCases.observeTenants()) { (leases, housingsById), tenants ->
                    val tenantsById = tenants.associateBy(Tenant::id)
                    leases.map { lease ->
                        LeaseListItem(
                            lease = lease,
                            tenantName = tenantsById[lease.tenantId]
                                ?.let { "${it.firstName} ${it.lastName}".trim() }
                                .orEmpty()
                                .ifBlank { "Locataire #${lease.tenantId}" },
                            housingLabel = housingsById[lease.housingId]
                                ?.address
                                ?.fullString()
                                .orEmpty()
                                .ifBlank { "Logement #${lease.housingId}" }
                        )
                    }
                }
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement des baux."
                        )
                    }
                }
                .collect { leases ->
                    allLeases = leases
                    val filtered = applySearch(_uiState.value.searchQuery, leases)
                    _uiState.update {
                        val searchResultEmpty = it.searchQuery.isNotBlank() && allLeases.isNotEmpty() && filtered.isEmpty()
                        it.copy(
                            isLoading = false,
                            leases = filtered,
                            isEmpty = allLeases.isEmpty(),
                            isSearchResultEmpty = searchResultEmpty,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun updateSearch(query: String) {
        val filtered = applySearch(query, allLeases)
        _uiState.update {
            it.copy(
                searchQuery = query,
                leases = filtered,
                isEmpty = allLeases.isEmpty(),
                isSearchResultEmpty = query.isNotBlank() && allLeases.isNotEmpty() && filtered.isEmpty(),
                errorMessage = null
            )
        }
    }

    private fun applySearch(query: String, leases: List<LeaseListItem>): List<LeaseListItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return leases
        return leases.filter { item ->
            val lease = item.lease
            lease.id.toString().contains(trimmed, ignoreCase = true) ||
                lease.startDateEpochDay.toString().contains(trimmed, ignoreCase = true) ||
                lease.endDateEpochDay?.toString()?.contains(trimmed, ignoreCase = true) == true ||
                lease.rentCents.toString().contains(trimmed, ignoreCase = true) ||
                item.tenantName.contains(trimmed, ignoreCase = true) ||
                item.housingLabel.contains(trimmed, ignoreCase = true)
        }
    }
}
