package com.florent.location.ui.sync

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.florent.location.data.sync.HousingSyncRepository

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Error(val message: String) : SyncState
}

class HousingSyncManager(
    private val repo: HousingSyncRepository,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state

    private var job: Job? = null

    fun requestSync(reason: String, debounceMs: Long = 800) {
        job?.cancel()
        job = scope.launch {
            delay(debounceMs)
            syncNow(reason)
        }
    }

    fun syncNow(reason: String) {
        scope.launch {
            _state.value = SyncState.Syncing
            runCatching { repo.syncOnce() }
                .onSuccess { _state.value = SyncState.Idle }
                .onFailure { _state.value = SyncState.Error(it.message ?: "Sync failed") }
        }
    }
}
