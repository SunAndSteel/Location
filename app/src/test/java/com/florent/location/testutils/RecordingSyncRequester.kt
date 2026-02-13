package com.florent.location.testutils

import com.florent.location.presentation.sync.HousingSyncRequester

class RecordingSyncRequester : HousingSyncRequester {
    val reasons = mutableListOf<String>()

    override fun requestSync(reason: String, debounceMs: Long) {
        reasons += reason
    }
}
