package com.florent.location.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.usecase.housing.ObserveHousingSituation
import com.florent.location.domain.usecase.housing.HousingUseCases
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
    val housings: List<HousingListItem> = emptyList(),
    val isEmpty: Boolean = false,
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
}

/**
 * ViewModel qui orchestre les cas d'usage liés aux logements.
 */
class HousingListViewModel(
    private val useCases: HousingUseCases,
    private val observeHousingSituation: ObserveHousingSituation
) : ViewModel() {

    private val _uiState = MutableStateFlow(HousingListUiState())
    val uiState: StateFlow<HousingListUiState> = _uiState

    init {
        observeHousings()
    }

    fun onEvent(event: HousingListUiEvent) {
        when (event) {
            is HousingListUiEvent.CreateHousing -> createHousing(event.housing)
            is HousingListUiEvent.DeleteHousing -> deleteHousing(event.id)
        }
    }

    private fun observeHousings() {
        viewModelScope.launch {
            useCases.observeHousings()
                .flatMapLatest { housings ->
                    if (housings.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        combine(
                            housings.map { housing ->
                                observeHousingSituation(housing)
                                    .map { situation -> HousingListItem(housing, situation) }
                            }
                        ) { items -> items.toList() }
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
                .collect { housings ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            housings = housings,
                            isEmpty = housings.isEmpty(),
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
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun deleteHousing(id: Long) {
        viewModelScope.launch {
            try {
                useCases.deleteHousing(id)
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }
}
