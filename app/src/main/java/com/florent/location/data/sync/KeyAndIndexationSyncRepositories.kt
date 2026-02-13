package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.IndexationEventDao
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.LeaseDao
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
        pushDirty()
        pullUpdates()
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = keyDao.getDirty()
        if (dirty.isEmpty()) return

        dirty.filter { it.isDeleted }.forEach { entity ->
            try {
                supabase.from("keys").delete {
                    filter {
                        filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                        filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
                    }
                }
            } finally {
                keyDao.deleteById(entity.id)
            }
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

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = toServerCursorIso(keyDao.getMaxServerUpdatedAtOrNull())

        val rows = supabase.from("keys").select {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
            }
        }.decodeList<KeyRow>()

        val entities = rows.mapNotNull { row ->
            val housing = housingDao.getByRemoteId(row.housingRemoteId) ?: return@mapNotNull null
            val existing = keyDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(existing?.id ?: 0L, housing.id)
        }
        if (entities.isNotEmpty()) {
            keyDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochSeconds?.let { keyDao.markClean(e.remoteId, it) } }
        }

        val remoteIds = supabase.from("keys").select {
            filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
        }.decodeList<KeyRow>().map { it.remoteId }.toSet()
        keyDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach { keyDao.hardDeleteByRemoteId(it) }
    }
}

class IndexationEventSyncRepository(
    private val supabase: SupabaseClient,
    private val indexationEventDao: IndexationEventDao,
    private val leaseDao: LeaseDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        pushDirty()
        pullUpdates()
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = indexationEventDao.getDirty()
        if (dirty.isEmpty()) return

        dirty.filter { it.isDeleted }.forEach { entity ->
            try {
                supabase.from("indexation_events").delete {
                    filter {
                        filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                        filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
                    }
                }
            } finally {
                indexationEventDao.hardDeleteByRemoteId(entity.remoteId)
            }
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

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = toServerCursorIso(indexationEventDao.getMaxServerUpdatedAtOrNull())

        val rows = supabase.from("indexation_events").select {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
            }
        }.decodeList<IndexationEventRow>()

        val entities = rows.mapNotNull { row ->
            val lease = leaseDao.getByRemoteId(row.leaseRemoteId) ?: return@mapNotNull null
            val existing = indexationEventDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(existing?.id ?: 0L, lease.id)
        }
        if (entities.isNotEmpty()) {
            indexationEventDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochSeconds?.let { indexationEventDao.markClean(e.remoteId, it) } }
        }

        val remoteIds = supabase.from("indexation_events").select {
            filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
        }.decodeList<IndexationEventRow>().map { it.remoteId }.toSet()
        indexationEventDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach { indexationEventDao.hardDeleteByRemoteId(it) }
    }
}
