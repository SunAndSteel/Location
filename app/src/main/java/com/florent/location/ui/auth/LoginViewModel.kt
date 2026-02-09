package com.florent.location.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.data.auth.AuthRepository
import com.florent.location.ui.sync.HousingSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginUiEvent {
    data class EmailChanged(val value: String) : LoginUiEvent
    data class PasswordChanged(val value: String) : LoginUiEvent
    data object Submit : LoginUiEvent
    data object ErrorConsumed : LoginUiEvent
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val syncManager: HousingSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEvent(event: LoginUiEvent, onSuccess: () -> Unit) {
        when (event) {
            is LoginUiEvent.EmailChanged ->
                _uiState.value = _uiState.value.copy(email = event.value, errorMessage = null)

            is LoginUiEvent.PasswordChanged ->
                _uiState.value = _uiState.value.copy(password = event.value, errorMessage = null)

            LoginUiEvent.ErrorConsumed ->
                _uiState.value = _uiState.value.copy(errorMessage = null)

            LoginUiEvent.Submit -> {
                val s = _uiState.value
                if (s.email.isBlank() || s.password.isBlank()) {
                    _uiState.value = s.copy(errorMessage = "Email et mot de passe requis.")
                    return
                }

                viewModelScope.launch {
                    _uiState.value = s.copy(isLoading = true, errorMessage = null)
                    runCatching {
                        authRepository.signIn(s.email.trim(), s.password)
                    }.onSuccess {
                        syncManager.requestSync("login", debounceMs = 0)
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        onSuccess()
                    }.onFailure { t ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = t.message ?: "Connexion impossible."
                        )
                    }
                }
            }
        }
    }
}
