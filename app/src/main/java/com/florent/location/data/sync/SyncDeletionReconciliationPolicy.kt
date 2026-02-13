package com.florent.location.data.sync

import java.util.concurrent.ConcurrentHashMap

internal class SyncDeletionReconciliationPolicy(
    private val fullReconciliationIntervalMs: Long = DEFAULT_FULL_RECONCILIATION_INTERVAL_MS,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) {
    private val lastFullReconciliationByRepository = ConcurrentHashMap<String, Long>()
    private val forcedRepositoryTags = mutableSetOf<String>()

    fun shouldRunFullReconciliation(repositoryTag: String): Boolean {
        synchronized(this) {
            if (forcedRepositoryTags.remove(repositoryTag)) {
                lastFullReconciliationByRepository[repositoryTag] = nowProvider()
                return true
            }

            val now = nowProvider()
            val last = lastFullReconciliationByRepository[repositoryTag]
            val shouldRun = last == null || now - last >= fullReconciliationIntervalMs
            if (shouldRun) {
                lastFullReconciliationByRepository[repositoryTag] = now
            }
            return shouldRun
        }
    }

    fun forceNextFullReconciliation(repositoryTag: String) {
        synchronized(this) {
            forcedRepositoryTags += repositoryTag
        }
    }

    companion object {
        const val DEFAULT_FULL_RECONCILIATION_INTERVAL_MS: Long = 24 * 60 * 60 * 1000L
    }
}

internal object SyncDeletionReconciliation {
    val policy = SyncDeletionReconciliationPolicy()
}
