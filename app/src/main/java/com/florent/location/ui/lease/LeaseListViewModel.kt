package com.florent.location.ui.lease

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Bail
import com.florent.location.domain.usecase.bail.BailUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LeaseListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val bails: List<Bail> = emptyList(),
    val isEmpty: Boolean = false,
    val isSearchResultEmpty: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LeaseListUiEvent {
    data class SearchQueryChanged(val value: String) : LeaseListUiEvent
}

class LeaseListViewModel(
    private val useCases: BailUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaseListUiState())
    val uiState: StateFlow<LeaseListUiState> = _uiState

    private var allBails: List<Bail> = emptyList()

    init {
        observeBails()
    }

    fun onEvent(event: LeaseListUiEvent) {
        when (event) {
            is LeaseListUiEvent.SearchQueryChanged -> updateSearch(event.value)
        }
    }

    private fun observeBails() {
        viewModelScope.launch {
            useCases.observeBails()
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

    private fun applySearch(query: String, bails: List<Bail>): List<Bail> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return bails
        return bails.filter { bail ->
            bail.id.toString().contains(trimmed, ignoreCase = true) ||
                bail.startDateEpochDay.toString().contains(trimmed, ignoreCase = true) ||
                bail.endDateEpochDay?.toString()?.contains(trimmed, ignoreCase = true) == true ||
                bail.rentCents.toString().contains(trimmed, ignoreCase = true)
        }
    }
}
