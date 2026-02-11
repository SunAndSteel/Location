package com.florent.location.ui.sync

import kotlinx.coroutines.flow.StateFlow

sealed interface SyncState {
    data object Idle : SyncState
    data object Syncing : SyncState
    data class Error(val message: String) : SyncState
}

interface HousingSyncRequester {
    fun requestSync(reason: String, debounceMs: Long = 800)
}

/**
 * Expose l'état de synchronisation consommé par l'UI globale (AuthGate).
 */
interface HousingSyncStateObserver {
    val state: StateFlow<SyncState>
    fun consumeError()
}
