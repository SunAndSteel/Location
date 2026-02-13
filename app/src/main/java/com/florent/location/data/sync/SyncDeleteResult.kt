package com.florent.location.data.sync

sealed interface SyncDeleteResult {
    data object Success : SyncDeleteResult
    data class Failure(val entityType: String, val remoteId: String, val reason: String) : SyncDeleteResult
}

class SyncDeleteFailuresException(
    val failures: List<SyncDeleteResult.Failure>
) : IllegalStateException(
    failures.joinToString(separator = "\n") { failure ->
        "${failure.entityType} delete failed for ${failure.remoteId}: ${failure.reason}"
    }
)

