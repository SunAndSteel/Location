package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.data.db.dao.HousingDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class HousingSyncRepository(
    private val supabase: SupabaseClient,
    private val housingDao: HousingDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        val startedAt = System.currentTimeMillis()
        pushDirty()
        pullUpdates()
        Log.i("HousingSyncRepository", "syncOnce completed durationMs=${System.currentTimeMillis() - startedAt}")
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = housingDao.getDirty()
        if (dirty.isEmpty()) return

        dirty.filter { it.isDeleted }.forEach { entity ->
            deleteDeletedHousing(entity, user.id)
        }

        val payload = dirty.filterNot { it.isDeleted }.map { it.toRow(userId = user.id) }
        if (payload.isNotEmpty()) {
            supabase.from("housings").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            dirty.filterNot { it.isDeleted }.forEach { housingDao.markClean(it.remoteId, null) }
        }
    }


    internal suspend fun deleteDeletedHousing(entity: HousingEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("housings").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }) {
        try {
            remoteDelete()
            housingDao.deleteById(entity.id)
        } catch (e: Exception) {
            Log.e("HousingSyncRepository", "Failed to delete remote housing ${entity.remoteId}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = toServerCursorIso(housingDao.getMaxServerUpdatedAtOrNull())

        val updatesResult = fetchAllPagedWithMetrics(tag = "HousingSyncRepository", pageLabel = "pullUpdates") { from, to ->
            supabase.from("housings").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                    if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                }
                range(from.toLong(), to.toLong())
            }
                .decodeList<HousingRow>()
        }
        val rows = updatesResult.rows

        val entities = rows.map { row ->
            val existing = housingDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(localId = existing?.id ?: 0L, existingCreatedAtMillis = existing?.createdAt)
        }
        if (entities.isNotEmpty()) {
            housingDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochMillis?.let { housingDao.markClean(e.remoteId, it) } }
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
