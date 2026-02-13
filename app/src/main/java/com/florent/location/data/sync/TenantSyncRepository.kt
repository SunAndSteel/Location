package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.entity.TenantEntity
import com.florent.location.data.db.dao.TenantDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TenantSyncRepository(
    private val supabase: SupabaseClient,
    private val tenantDao: TenantDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        val startedAt = System.currentTimeMillis()
        pushDirty()
        pullUpdates()
        Log.i("TenantSyncRepository", "syncOnce completed durationMs=${System.currentTimeMillis() - startedAt}")
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = tenantDao.getDirty()
        if (dirty.isEmpty()) return

        val deleted = dirty.filter { it.isDeleted }
        deleted.forEach { entity ->
            deleteDeletedTenant(entity, user.id)
        }

        val payload = dirty.filterNot { it.isDeleted }.map { it.toRow(userId = user.id) }
        if (payload.isNotEmpty()) {
            supabase.from("tenants").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            dirty.filterNot { it.isDeleted }.forEach { tenantDao.markClean(it.remoteId, null) }
        }
    }


    internal suspend fun deleteDeletedTenant(entity: TenantEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("tenants").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }) {
        try {
            remoteDelete()
            tenantDao.deleteById(entity.id)
        } catch (e: Exception) {
            Log.e("TenantSyncRepository", "Failed to delete remote tenant ${entity.remoteId}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = toServerCursorIso(tenantDao.getMaxServerUpdatedAtOrNull())

        val updatesResult = fetchAllPagedWithMetrics(tag = "TenantSyncRepository", pageLabel = "pullUpdates") { from, to ->
            supabase.from("tenants").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                    if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                }
                range(from.toLong(), to.toLong())
            }
                .decodeList<TenantRow>()
        }
        val rows = updatesResult.rows

        val entities = rows.map { row ->
            val existing = tenantDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(localId = existing?.id ?: 0L, existingCreatedAtMillis = existing?.createdAt)
        }
        if (entities.isNotEmpty()) {
            tenantDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochMillis?.let { tenantDao.markClean(e.remoteId, it) } }
        }

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
            "pullUpdates completed updatedVolume=${rows.size} updatedPages=${updatesResult.pageCount} updatedDurationMs=${updatesResult.durationMs} hardDeleted=$hardDeleted fullReconciliation=$shouldRunFullReconciliation"
        )
    }
}
