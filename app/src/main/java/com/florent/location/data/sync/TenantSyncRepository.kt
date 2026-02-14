package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.entity.TenantEntity
import com.florent.location.data.db.dao.TenantDao
import com.florent.location.data.db.dao.SyncCursorDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TenantSyncRepository(
    private val supabase: SupabaseClient,
    private val tenantDao: TenantDao,
    private val syncCursorDao: SyncCursorDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        val startedAt = System.currentTimeMillis()
        val deleteFailures = pushDirty()
        if (deleteFailures.isNotEmpty()) {
            throw SyncDeleteFailuresException(deleteFailures)
        }
        pullUpdates()
        Log.i("TenantSyncRepository", "syncOnce completed durationMs=${System.currentTimeMillis() - startedAt}")
    }

    private suspend fun pushDirty(): List<SyncDeleteResult.Failure> {
        val user = supabase.auth.currentUserOrNull() ?: return emptyList()
        val dirty = tenantDao.getDirty()
        if (dirty.isEmpty()) return emptyList()

        val deleteFailures = mutableListOf<SyncDeleteResult.Failure>()
        val deleted = dirty.filter { it.isDeleted }
        deleted.forEach { entity ->
            when (val result = deleteDeletedTenant(entity, user.id)) {
                SyncDeleteResult.Success -> Unit
                is SyncDeleteResult.Failure -> deleteFailures += result
            }
        }

        val payload = dirty.filterNot { it.isDeleted }.map { it.toRow(userId = user.id) }
        if (payload.isNotEmpty()) {
            supabase.from("tenants").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            dirty.filterNot { it.isDeleted }.forEach { tenantDao.markClean(it.remoteId, null) }
        }

        return deleteFailures
    }


    internal suspend fun deleteDeletedTenant(entity: TenantEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("tenants").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }): SyncDeleteResult {
        return try {
            remoteDelete()
            tenantDao.deleteById(entity.id)
            SyncDeleteResult.Success
        } catch (e: Exception) {
            Log.e("TenantSyncRepository", "Failed to delete remote tenant ${entity.remoteId}", e)
            SyncDeleteResult.Failure(
                entityType = "Tenant",
                remoteId = entity.remoteId,
                reason = e.message ?: "Unknown delete error"
            )
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val cursor = syncCursorDao.getByKey(user.id, "tenants")?.toCompositeCursor()
        var invalidUpdatedAtCount = 0

        val updatesResult = processKeysetPagedWithMetrics(
            tag = "TenantSyncRepository",
            pageLabel = "pullUpdates",
            initialCursor = cursor,
            fetchPage = { updatedAtFromInclusiveIso, limit ->
                supabase.from("tenants").select {
                    filter {
                        filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                        if (updatedAtFromInclusiveIso != null) {
                            filter(column = "updated_at", operator = FilterOperator.GTE, value = updatedAtFromInclusiveIso)
                        }
                    }
                    order(column = "updated_at", order = Order.ASCENDING)
                    order(column = "remote_id", order = Order.ASCENDING)
                    limit(limit.toLong())
                }.decodeList<TenantRow>()
            },
            extractUpdatedAt = { it.updatedAt },
            extractRemoteId = { it.remoteId },
            processPage = { rows ->
                invalidUpdatedAtCount += rows.count { row -> hasInvalidUpdatedAt(row.updatedAt) }
                val entities = rows.map { row ->
                    val existing = tenantDao.getByRemoteId(row.remoteId)
                    row.toEntityPreservingLocalId(localId = existing?.id ?: 0L, existingCreatedAtMillis = existing?.createdAt)
                }
                if (entities.isNotEmpty()) {
                    tenantDao.upsertAll(entities)
                    entities.forEach { e -> e.serverUpdatedAtEpochMillis?.let { tenantDao.markClean(e.remoteId, it) } }
                }
            },
            onCursorAdvanced = { nextCursor ->
                syncCursorDao.upsert(nextCursor.toEntity(user.id, "tenants"))
            }
        )

        var hardDeleted = 0
        val shouldRunFullReconciliation = SyncDeletionReconciliation.policy.shouldRunFullReconciliation("TenantSyncRepository")
        if (shouldRunFullReconciliation) {
            val remoteIdsResult = fetchAllPagedWithMetrics(tag = "TenantSyncRepository", pageLabel = "pullRemoteIds") { from, to ->
                supabase.from("tenants").select {
                    filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
                    range(from.toLong(), to.toLong())
                }.decodeList<TenantRow>()
            }
            val remoteIds = remoteIdsResult.rows.map { it.remoteId }.toSet()
            tenantDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach {
                tenantDao.hardDeleteByRemoteId(it)
                hardDeleted++
            }
        } else {
            Log.d("TenantSyncRepository", "Skipping full reconciliation for this sync cycle")
        }

        Log.i(
            "TenantSyncRepository",
            "pullUpdates completed updatedVolume=${updatesResult.processedCount} invalidUpdatedAtCount=$invalidUpdatedAtCount updatedPages=${updatesResult.pageCount} updatedDurationMs=${updatesResult.durationMs} hardDeleted=$hardDeleted fullReconciliation=$shouldRunFullReconciliation"
        )
    }
}
