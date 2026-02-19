package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Bail
import com.florent.location.domain.model.Housing
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
    val bail: Bail,
    val tenantName: String,
    val housingLabel: String
)

data class LeaseListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val bails: List<LeaseListItem> = emptyList(),
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

    private var allBails: List<LeaseListItem> = emptyList()

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
                .combine(housingUseCases.observeHousings()) { bails, housings ->
                    bails to housings.associateBy(Housing::id)
                }
                .combine(tenantUseCases.observeTenants()) { (bails, housingsById), tenants ->
                    val tenantsById = tenants.associateBy(Tenant::id)
                    bails.map { bail ->
                        LeaseListItem(
                            bail = bail,
                            tenantName = tenantsById[bail.tenantId]
                                ?.let { "${it.firstName} ${it.lastName}".trim() }
                                .orEmpty()
                                .ifBlank { "Locataire #${bail.tenantId}" },
                            housingLabel = housingsById[bail.housingId]
                                ?.address
                                ?.fullString()
                                .orEmpty()
                                .ifBlank { "Logement #${bail.housingId}" }
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
                .collect { bails ->
                    allBails = bails
                    val filtered = applySearch(_uiState.value.searchQuery, bails)
                    _uiState.update {
                        val searchResultEmpty = it.searchQuery.isNotBlank() && allBails.isNotEmpty() && filtered.isEmpty()
                        it.copy(
                            isLoading = false,
                            bails = filtered,
                            isEmpty = allBails.isEmpty(),
                            isSearchResultEmpty = searchResultEmpty,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun updateSearch(query: String) {
        val filtered = applySearch(query, allBails)
        _uiState.update {
            it.copy(
                searchQuery = query,
                bails = filtered,
                isEmpty = allBails.isEmpty(),
                isSearchResultEmpty = query.isNotBlank() && allBails.isNotEmpty() && filtered.isEmpty(),
                errorMessage = null
            )
        }
    }

    private fun applySearch(query: String, bails: List<LeaseListItem>): List<LeaseListItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return bails
        return bails.filter { item ->
            val bail = item.bail
            bail.id.toString().contains(trimmed, ignoreCase = true) ||
                bail.startDateEpochDay.toString().contains(trimmed, ignoreCase = true) ||
                bail.endDateEpochDay?.toString()?.contains(trimmed, ignoreCase = true) == true ||
                bail.rentCents.toString().contains(trimmed, ignoreCase = true) ||
                item.tenantName.contains(trimmed, ignoreCase = true) ||
                item.housingLabel.contains(trimmed, ignoreCase = true)
        }
    }
}
