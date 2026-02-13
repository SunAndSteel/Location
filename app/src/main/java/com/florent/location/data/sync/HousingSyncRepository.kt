package com.florent.location.data.sync

import com.florent.location.data.db.dao.HousingDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

class HousingSyncRepository(
    private val supabase: SupabaseClient,
    private val housingDao: HousingDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        pushDirty()
        pullUpdates()
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = housingDao.getDirty()
        if (dirty.isEmpty()) return

        dirty.filter { it.isDeleted }.forEach { entity ->
            try {
                supabase.from("housings").delete {
                    filter {
                        filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                        filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
                    }
                }
            } finally {
                housingDao.deleteById(entity.id)
            }
        }

        val payload = dirty.filterNot { it.isDeleted }.map { it.toRow(userId = user.id) }
        if (payload.isNotEmpty()) {
            supabase.from("housings").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = housingDao.getMaxServerUpdatedAtOrNull()?.let { Instant.ofEpochSecond(it).toString() }

        val rows = supabase.from("housings").select {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
            }
        }.decodeList<HousingRow>()

        val entities = rows.map { row ->
            val existing = housingDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(localId = existing?.id ?: 0L, existingCreatedAtMillis = existing?.createdAt)
        }
        if (entities.isNotEmpty()) {
            housingDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochSeconds?.let { housingDao.markClean(e.remoteId, it) } }
        }

        val remoteIds = supabase.from("housings").select {
            filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
        }.decodeList<HousingRow>().map { it.remoteId }.toSet()

        housingDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach { housingDao.hardDeleteByRemoteId(it) }
    }
}
