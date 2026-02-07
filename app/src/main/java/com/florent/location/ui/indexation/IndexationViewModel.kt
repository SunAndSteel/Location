package com.florent.location.ui.indexation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.domain.model.UpcomingIndexation
import com.florent.location.domain.usecase.indexation.IndexationUseCases
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IndexationUiState(
    val isLoading: Boolean = true,
    val upcomingIndexations: List<UpcomingIndexation> = emptyList(),
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
)

class IndexationViewModel(
    private val useCases: IndexationUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(IndexationUiState())
    val uiState: StateFlow<IndexationUiState> = _uiState

    init {
        observeUpcomingIndexations()
    }

    private fun observeUpcomingIndexations() {
        val todayEpochDay = LocalDate.now(ZoneId.of("Europe/Brussels")).toEpochDay()
        viewModelScope.launch {
            useCases.observeUpcomingIndexations(todayEpochDay)
                .onStart {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "Erreur lors du chargement des indexations."
                        )
                    }
                }
                .collect { indexations ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            upcomingIndexations = indexations,
                            isEmpty = indexations.isEmpty(),
                            errorMessage = null
                        )
                    }
                }
        }
    }
}
