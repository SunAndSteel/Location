package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.IndexationEventDao
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.data.db.entity.IndexationEventEntity
import com.florent.location.data.db.entity.KeyEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KeySyncRepository(
    private val supabase: SupabaseClient,
    private val keyDao: KeyDao,
    private val housingDao: HousingDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        val startedAt = System.currentTimeMillis()
        pushDirty()
        pullUpdates()
        Log.i("KeySyncRepository", "syncOnce completed durationMs=${System.currentTimeMillis() - startedAt}")
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = keyDao.getDirty()
        if (dirty.isEmpty()) return

        dirty.filter { it.isDeleted }.forEach { entity ->
            deleteDeletedKey(entity, user.id)
        }

        val payload = dirty.filterNot { it.isDeleted }.mapNotNull { entity ->
            val housing = housingDao.getById(entity.housingId) ?: return@mapNotNull null
            entity to entity.toRow(user.id, housing.remoteId)
        }

        if (payload.isNotEmpty()) {
            supabase.from("keys").upsert(payload.map { it.second }) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            payload.forEach { keyDao.markClean(it.first.remoteId, null) }
        }
    }

    internal suspend fun deleteDeletedKey(entity: KeyEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("keys").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }) {
        try {
            remoteDelete()
            keyDao.deleteById(entity.id)
        } catch (e: Exception) {
            Log.e("KeySyncRepository", "Failed to delete remote key ${entity.remoteId}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = toServerCursorIso(keyDao.getMaxServerUpdatedAtOrNull())

        val updatesResult = fetchAllPagedWithMetrics(tag = "KeySyncRepository", pageLabel = "pullUpdates") { from, to ->
            supabase.from("keys").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                    if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                }
                range(from.toLong(), to.toLong())
            }.decodeList<KeyRow>()
        }
        val rows = updatesResult.rows.sortedWith(compareBy<KeyRow> { parseServerEpochMillis(it.updatedAt) ?: Long.MAX_VALUE }.thenBy { it.remoteId })

        val resolution = mapRowsStoppingAtDependencyGap(rows,
            mapRow = { row ->
                val housing = housingDao.getByRemoteId(row.housingRemoteId)
                if (housing == null) null
                else {
                    val existing = keyDao.getByRemoteId(row.remoteId)
                    row.toEntityPreservingLocalId(existing?.id ?: 0L, housing.id)
                }
            },
            onMissingDependency = { row ->
                Log.w(
                    "KeySyncRepository",
                    "Stopping incremental key pull on dependency gap: remote_id=${row.remoteId}, missing=housing_remote_id=${row.housingRemoteId}"
                )
            }
        )
        if (resolution.mapped.isNotEmpty()) {
            keyDao.upsertAll(resolution.mapped)
            resolution.mapped.forEach { e -> e.serverUpdatedAtEpochMillis?.let { keyDao.markClean(e.remoteId, it) } }
        }

        var hardDeleted = 0
        val shouldRunFullReconciliation = SyncDeletionReconciliation.policy.shouldRunFullReconciliation("KeySyncRepository")
        if (shouldRunFullReconciliation) {
            val remoteIdsResult = fetchAllPagedWithMetrics(tag = "KeySyncRepository", pageLabel = "pullRemoteIds") { from, to ->
                supabase.from("keys").select {
                    filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
                    range(from.toLong(), to.toLong())
                }.decodeList<KeyRow>()
            }
            val remoteIds = remoteIdsResult.rows.map { it.remoteId }.toSet()
            keyDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach {
                keyDao.hardDeleteByRemoteId(it)
                hardDeleted++
            }
        } else {
            Log.d("KeySyncRepository", "Skipping full reconciliation for this sync cycle")
        }

        Log.i(
            "KeySyncRepository",
            "pullUpdates completed updatedVolume=${rows.size} updatedPages=${updatesResult.pageCount} updatedDurationMs=${updatesResult.durationMs} hardDeleted=$hardDeleted fullReconciliation=$shouldRunFullReconciliation"
        )
    }
}

class IndexationEventSyncRepository(
    private val supabase: SupabaseClient,
    private val indexationEventDao: IndexationEventDao,
    private val leaseDao: LeaseDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        val startedAt = System.currentTimeMillis()
        pushDirty()
        pullUpdates()
        Log.i("IndexationEventSyncRepository", "syncOnce completed durationMs=${System.currentTimeMillis() - startedAt}")
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = indexationEventDao.getDirty()
        if (dirty.isEmpty()) return

        dirty.filter { it.isDeleted }.forEach { entity ->
            deleteDeletedIndexationEvent(entity, user.id)
        }

        val payload = dirty.filterNot { it.isDeleted }.mapNotNull { entity ->
            val lease = leaseDao.getById(entity.leaseId)
            if (lease == null) {
                Log.w("IndexationEventSyncRepository", "Missing lease for event ${entity.id}")
                null
            } else entity to entity.toRow(user.id, lease.remoteId)
        }

        if (payload.isNotEmpty()) {
            supabase.from("indexation_events").upsert(payload.map { it.second }) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            payload.forEach { indexationEventDao.markClean(it.first.remoteId, null) }
        }
    }

    internal suspend fun deleteDeletedIndexationEvent(entity: IndexationEventEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("indexation_events").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }) {
        try {
            remoteDelete()
            indexationEventDao.hardDeleteByRemoteId(entity.remoteId)
        } catch (e: Exception) {
            Log.e("IndexationEventSyncRepository", "Failed to delete remote indexation event ${entity.remoteId}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = toServerCursorIso(indexationEventDao.getMaxServerUpdatedAtOrNull())

        val updatesResult = fetchAllPagedWithMetrics(tag = "IndexationEventSyncRepository", pageLabel = "pullUpdates") { from, to ->
            supabase.from("indexation_events").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                    if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                }
                range(from.toLong(), to.toLong())
            }.decodeList<IndexationEventRow>()
        }
        val rows = updatesResult.rows.sortedWith(compareBy<IndexationEventRow> { parseServerEpochMillis(it.updatedAt) ?: Long.MAX_VALUE }.thenBy { it.remoteId })

        val resolution = mapRowsStoppingAtDependencyGap(rows,
            mapRow = { row ->
                val lease = leaseDao.getByRemoteId(row.leaseRemoteId)
                if (lease == null) null
                else {
                    val existing = indexationEventDao.getByRemoteId(row.remoteId)
                    row.toEntityPreservingLocalId(existing?.id ?: 0L, lease.id)
                }
            },
            onMissingDependency = { row ->
                Log.w(
                    "IndexationEventSyncRepository",
                    "Stopping incremental indexation pull on dependency gap: remote_id=${row.remoteId}, missing=lease_remote_id=${row.leaseRemoteId}"
                )
            }
        )
        if (resolution.mapped.isNotEmpty()) {
            indexationEventDao.upsertAll(resolution.mapped)
            resolution.mapped.forEach { e -> e.serverUpdatedAtEpochMillis?.let { indexationEventDao.markClean(e.remoteId, it) } }
        }

        var hardDeleted = 0
        val shouldRunFullReconciliation = SyncDeletionReconciliation.policy.shouldRunFullReconciliation("IndexationEventSyncRepository")
        if (shouldRunFullReconciliation) {
            val remoteIdsResult = fetchAllPagedWithMetrics(tag = "IndexationEventSyncRepository", pageLabel = "pullRemoteIds") { from, to ->
                supabase.from("indexation_events").select {
                    filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
                    range(from.toLong(), to.toLong())
                }.decodeList<IndexationEventRow>()
            }
            val remoteIds = remoteIdsResult.rows.map { it.remoteId }.toSet()
            indexationEventDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach {
                indexationEventDao.hardDeleteByRemoteId(it)
                hardDeleted++
            }
        } else {
            Log.d("IndexationEventSyncRepository", "Skipping full reconciliation for this sync cycle")
        }

        Log.i(
            "IndexationEventSyncRepository",
            "pullUpdates completed updatedVolume=${rows.size} updatedPages=${updatesResult.pageCount} updatedDurationMs=${updatesResult.durationMs} hardDeleted=$hardDeleted fullReconciliation=$shouldRunFullReconciliation"
        )
    }
}
