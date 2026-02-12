package com.florent.location.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.usecase.housing.ObserveHousingSituation
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.ui.sync.HousingSyncRequester
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

/**
 * État d'UI pour la liste des logements.
 */
data class HousingListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val housings: List<HousingListItem> = emptyList(),
    val isEmpty: Boolean = false,
    val isSearchResultEmpty: Boolean = false,
    val errorMessage: String? = null
)

data class HousingListItem(
    val housing: Housing,
    val situation: HousingSituation
)

/**
 * Événements utilisateur pour la liste des logements.
 */
sealed interface HousingListUiEvent {
    data class CreateHousing(val housing: Housing) : HousingListUiEvent
    data class DeleteHousing(val id: Long) : HousingListUiEvent
    data class SearchQueryChanged(val query: String) : HousingListUiEvent
}

/**
 * ViewModel qui orchestre les cas d'usage liés aux logements.
 */
class HousingListViewModel(
    private val useCases: HousingUseCases,
    private val observeHousingSituation: ObserveHousingSituation,
    private val syncManager: HousingSyncRequester
) : ViewModel() {

    private val _uiState = MutableStateFlow(HousingListUiState())
    val uiState: StateFlow<HousingListUiState> = _uiState
    private val searchQuery = MutableStateFlow("")

    init {
        observeHousings()
    }

    fun onEvent(event: HousingListUiEvent) {
        when (event) {
            is HousingListUiEvent.CreateHousing -> createHousing(event.housing)
            is HousingListUiEvent.DeleteHousing -> deleteHousing(event.id)
            is HousingListUiEvent.SearchQueryChanged -> updateSearchQuery(event.query)
        }
    }

    private fun observeHousings() {
        viewModelScope.launch {
            combine(useCases.observeHousings(), searchQuery) { housings, query ->
                val filtered = filterHousings(housings, query)
                Triple(query, housings.isNotEmpty(), filtered)
            }.flatMapLatest { (query, hasAnyHousing, housings) ->
                if (housings.isEmpty()) {
                    flowOf(Triple(query, hasAnyHousing, emptyList()))
                } else {
                    combine(
                        housings.map { housing ->
                            observeHousingSituation(housing)
                                .map { situation -> HousingListItem(housing, situation) }
                        }
                    ) { items -> Triple(query, hasAnyHousing, items.toList()) }
                }
            }
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement des logements."
                        )
                    }
                }
                .collect { (query, hasAnyHousing, housings) ->
                    _uiState.update {
                        val searchResultEmpty = query.isNotBlank() && hasAnyHousing && housings.isEmpty()
                        it.copy(
                            isLoading = false,
                            searchQuery = query,
                            housings = housings,
                            isEmpty = !hasAnyHousing,
                            isSearchResultEmpty = searchResultEmpty,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun createHousing(housing: Housing) {
        viewModelScope.launch {
            try {
                useCases.createHousing(housing)
                syncManager.requestSync("housing_create")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun deleteHousing(id: Long) {
        viewModelScope.launch {
            try {
                useCases.deleteHousing(id)
                syncManager.requestSync("housing_delete")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    private fun filterHousings(housings: List<Housing>, query: String): List<Housing> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            return housings
        }
        return housings.filter { housing ->
            val searchable = buildString {
                append(housing.address.fullString())
                append(" ")
                append(housing.address.city)
                append(" ")
                append(housing.address.street)
                append(" ")
                append(housing.address.number)
                append(" ")
                append(housing.address.zipCode)
                append(" ")
                append(housing.address.country)
                housing.buildingLabel?.let { append(" $it") }
                housing.internalNote?.let { append(" $it") }
                housing.mailboxLabel?.let { append(" $it") }
            }
            searchable.lowercase().contains(normalizedQuery)
        }
    }
}
