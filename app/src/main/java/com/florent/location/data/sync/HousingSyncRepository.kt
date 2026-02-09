package com.florent.location.data.sync

import com.florent.location.data.db.dao.HousingDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import io.github.jan.supabase.postgrest.query.filter.FilterOperator


class HousingSyncRepository(
    private val supabase: SupabaseClient,
    private val housingDao: HousingDao
) {
    private val mutex = Mutex() // évite 2 sync concurrentes

    suspend fun syncOnce() = mutex.withLock {
        pushDirty()
        pullUpdates()
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return

        val dirty = housingDao.getDirty()
        if (dirty.isEmpty()) return

        val payload = dirty.map { it.toRow(userId = user.id) }

        supabase.from("housings").upsert(payload) {
            onConflict = "remote_id"
            ignoreDuplicates = false
        }

        // Ne marque pas clean ici : on le fera après pull (car on veut updated_at serveur)
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return

        val sinceEpoch = housingDao.getMaxServerUpdatedAtOrNull()
        val sinceIso = sinceEpoch?.let { Instant.ofEpochSecond(it).toString() }

        val response = supabase.from("housings").select {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)

                if (sinceIso != null) {
                    filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                }
            }
        }

        val rows = response.decodeList<HousingRow>()
        if (rows.isEmpty()) return

        val entities = rows.map { row ->
            val existing = housingDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(localId = existing?.id ?: 0L)
        }

        housingDao.upsertAll(entities)

        // Bonus : si tu veux marquer clean explicitement (pas nécessaire car entity.dirty=false)
        entities.forEach { e ->
            val serverUpdated = e.serverUpdatedAtEpochSeconds
            if (serverUpdated != null) housingDao.markClean(e.remoteId, serverUpdated)
        }
    }
}
