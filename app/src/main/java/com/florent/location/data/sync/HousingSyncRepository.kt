package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.SyncCursorDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HousingSyncRepository(
    private val supabase: SupabaseClient,
    private val housingDao: HousingDao,
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
        Log.i("HousingSyncRepository", "syncOnce completed durationMs=${System.currentTimeMillis() - startedAt}")
    }

    private suspend fun pushDirty(): List<SyncDeleteResult.Failure> {
        val user = supabase.auth.currentUserOrNull() ?: return emptyList()
        val dirty = housingDao.getDirty()
        if (dirty.isEmpty()) return emptyList()

        val deleteFailures = mutableListOf<SyncDeleteResult.Failure>()

        dirty.filter { it.isDeleted }.forEach { entity ->
            when (val result = deleteDeletedHousing(entity, user.id)) {
                SyncDeleteResult.Success -> Unit
                is SyncDeleteResult.Failure -> deleteFailures += result
            }
        }

        val payload = dirty.filterNot { it.isDeleted }.map { it.toRow(userId = user.id) }
        if (payload.isNotEmpty()) {
            supabase.from("housings").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            dirty.filterNot { it.isDeleted }.forEach { housingDao.markClean(it.remoteId, null) }
        }

        return deleteFailures
    }


    internal suspend fun deleteDeletedHousing(entity: HousingEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("housings").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }): SyncDeleteResult {
        return try {
            remoteDelete()
            housingDao.deleteById(entity.id)
            SyncDeleteResult.Success
        } catch (e: Exception) {
            Log.e("HousingSyncRepository", "Failed to delete remote housing ${entity.remoteId}", e)
            SyncDeleteResult.Failure(
                entityType = "Housing",
                remoteId = entity.remoteId,
                reason = e.message ?: "Unknown delete error"
            )
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val cursor = syncCursorDao.getByKey("housings")?.toCompositeCursor()
        val sinceIso = cursor?.let { toServerCursorIso(it.updatedAtEpochMillis) }

        val updatesResult = fetchAllPagedWithMetrics(tag = "HousingSyncRepository", pageLabel = "pullUpdates") { from, to ->
            supabase.from("housings").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                    if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GTE, value = sinceIso)
                }
                order(column = "updated_at", order = Order.ASCENDING)
                order(column = "remote_id", order = Order.ASCENDING)
                range(from.toLong(), to.toLong())
            }
                .decodeList<HousingRow>()
        }
        val rows = updatesResult.rows.filter { row -> isAfterCursor(row.updatedAt, row.remoteId, cursor) }

        val entities = rows.map { row ->
            val existing = housingDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(localId = existing?.id ?: 0L, existingCreatedAtMillis = existing?.createdAt)
        }
        if (entities.isNotEmpty()) {
            housingDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochMillis?.let { housingDao.markClean(e.remoteId, it) } }
            entities.maxCompositeCursorOrNull { e ->
                e.serverUpdatedAtEpochMillis?.let { CompositeSyncCursor(updatedAtEpochMillis = it, remoteId = e.remoteId) }
            }?.let { syncCursorDao.upsert(it.toEntity("housings")) }
        }

        var hardDeleted = 0
        val shouldRunFullReconciliation = SyncDeletionReconciliation.policy.shouldRunFullReconciliation("HousingSyncRepository")
        if (shouldRunFullReconciliation) {
            val remoteIdsResult = fetchAllPagedWithMetrics(tag = "HousingSyncRepository", pageLabel = "pullRemoteIds") { from, to ->
                supabase.from("housings").select {
                    filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
                    range(from.toLong(), to.toLong())
                }.decodeList<HousingRow>()
            }
            val remoteIds = remoteIdsResult.rows.map { it.remoteId }.toSet()
            housingDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach {
                housingDao.hardDeleteByRemoteId(it)
                hardDeleted++
            }
        } else {
            Log.d("HousingSyncRepository", "Skipping full reconciliation for this sync cycle")
        }

        Log.i(
            "HousingSyncRepository",
            "pullUpdates completed updatedVolume=${rows.size} updatedPages=${updatesResult.pageCount} updatedDurationMs=${updatesResult.durationMs} hardDeleted=$hardDeleted fullReconciliation=$shouldRunFullReconciliation"
        )
    }
}
