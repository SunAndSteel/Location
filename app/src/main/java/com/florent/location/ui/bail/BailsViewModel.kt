package com.florent.location.ui.bail

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

data class BailsUiState(
    val isLoading: Boolean = true,
    val bails: List<Bail> = emptyList(),
    val isEmpty: Boolean = false,
    val errorMessage: String? = null
)

class BailsViewModel(
    private val useCases: BailUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(BailsUiState())
    val uiState: StateFlow<BailsUiState> = _uiState

    init {
        observeBails()
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            bails = bails,
                            isEmpty = bails.isEmpty(),
                            errorMessage = null
                        )
                    }
                }
        }
    }
}
