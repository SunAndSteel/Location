package com.florent.location.ui.housing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.usecase.housing.HousingUseCases
import com.florent.location.domain.usecase.housing.ObserveHousingSituation
import com.florent.location.presentation.sync.HousingSyncRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * État d'UI pour le détail d'un logement.
 */
data class HousingDetailUiState(
    val isLoading: Boolean = true,
    val housing: Housing? = null,
    val situation: HousingSituation? = null,
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Événements utilisateur pour le détail d'un logement.
 */
sealed interface HousingDetailUiEvent {
    data class UpdateHousing(val housing: Housing) : HousingDetailUiEvent
    data class DeleteHousing(val id: Long) : HousingDetailUiEvent
}

/**
 * ViewModel qui orchestre les cas d'usage liés à un logement.
 */
class HousingDetailViewModel(
    private val housingId: Long,
    private val housingUseCases: HousingUseCases,
    private val observeHousingSituation: ObserveHousingSituation,
    private val syncManager: HousingSyncRequester
) : ViewModel() {

    private val _uiState = MutableStateFlow(HousingDetailUiState())
    val uiState: StateFlow<HousingDetailUiState> = _uiState

    init {
        observeHousing()
    }

    fun onEvent(event: HousingDetailUiEvent) {
        when (event) {
            is HousingDetailUiEvent.UpdateHousing -> updateHousing(event.housing)
            is HousingDetailUiEvent.DeleteHousing -> deleteHousing(event.id)
        }
    }

    private fun observeHousing() {
        viewModelScope.launch {
            housingUseCases.observeHousing(housingId)
                .flatMapLatest { housing ->
                    if (housing == null) {
                        flowOf(Pair<Housing?, HousingSituation?>(null, null))
                    } else {
                        observeHousingSituation(housing).map { situation ->
                            housing to situation
                        }
                    }
                }
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Erreur lors du chargement du logement."
                        )
                    }
                }
                .collect { (housing, situation) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            housing = housing,
                            situation = situation,
                            isEmpty = housing == null,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    private fun updateHousing(housing: Housing) {
        viewModelScope.launch {
            try {
                housingUseCases.updateHousing(housing)
                syncManager.requestSync("housing_update")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    private fun deleteHousing(id: Long) {
        viewModelScope.launch {
            try {
                housingUseCases.deleteHousing(id)
                syncManager.requestSync("housing_delete")
            } catch (error: IllegalArgumentException) {
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }
}
