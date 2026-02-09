package com.florent.location.ui.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.florent.location.data.sync.HousingSyncRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Error(val message: String) : SyncState
}

interface HousingSyncRequester {
    fun requestSync(reason: String, debounceMs: Long = 800)
}

class HousingSyncManager(
    private val repo: HousingSyncRepository,
    private val scope: CoroutineScope
) : HousingSyncRequester {
    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state

    private var job: Job? = null
    private val syncMutex = Mutex()

    override fun requestSync(reason: String, debounceMs: Long) {
        job?.cancel()
        job = scope.launch {
            delay(debounceMs)
            syncNow(reason)
        }
    }

    fun syncNow(reason: String) {
        scope.launch {
            syncMutex.withLock {
                _state.value = SyncState.Syncing
                runCatching { repo.syncOnce() }
                    .onSuccess { _state.value = SyncState.Idle }
                    .onFailure { _state.value = SyncState.Error(it.message ?: "Sync failed") }
            }
        }
    }

    fun consumeError() {
        if (_state.value is SyncState.Error) {
            _state.value = SyncState.Idle
        }
    }
}
