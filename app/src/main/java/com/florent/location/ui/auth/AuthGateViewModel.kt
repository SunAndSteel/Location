package com.florent.location.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.florent.location.data.auth.AuthRepository
import com.florent.location.presentation.sync.HousingSyncRequester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface AuthGateState {
    data object Loading : AuthGateState
    data object Unauthenticated : AuthGateState
    data object Authenticated : AuthGateState
}

class AuthGateViewModel(
    private val authRepository: AuthRepository,
    private val syncManager: HousingSyncRequester
) : ViewModel() {

    private val _state = MutableStateFlow<AuthGateState>(AuthGateState.Loading)
    val state: StateFlow<AuthGateState> = _state

    init {
        viewModelScope.launch {
            val ok = runCatching { authRepository.restoreSession() }.getOrDefault(false)
            _state.value = if (ok) {
                // ✅ Sync après restore session
                syncManager.requestSync("restore", debounceMs = 0)
                AuthGateState.Authenticated
            } else {
                AuthGateState.Unauthenticated
            }
        }
    }

    fun onLoggedIn() {
        _state.value = AuthGateState.Authenticated
    }
}
