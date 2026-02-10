package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.IndexationEventDao
import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.LeaseDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

// ============================================================================
// KEY SYNC REPOSITORY
// ============================================================================

class KeySyncRepository(
    private val supabase: SupabaseClient,
    private val keyDao: KeyDao,
    private val housingDao: HousingDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        try {
            pushDirty()
            pullUpdates()
        } catch (e: Exception) {
            Log.e("KeySyncRepository", "Sync failed: ${e.message}", e)
            throw e
        }
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            Log.w("KeySyncRepository", "User not authenticated - skipping push")
            return
        }

        val dirty = keyDao.getDirty()
        if (dirty.isEmpty()) {
            Log.d("KeySyncRepository", "No dirty records to push")
            return
        }

        val payload = dirty.mapNotNull { entity ->
            val housing = housingDao.getById(entity.housingId)
            if (housing == null) {
                Log.e("KeySyncRepository", "Missing housing for key ${entity.id}")
                return@mapNotNull null
            }

            entity.toRow(userId = user.id, housingRemoteId = housing.remoteId)
        }

        if (payload.isEmpty()) return

        try {
            supabase.from("keys").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            Log.d("KeySyncRepository", "Successfully pushed ${payload.size} records")
        } catch (e: Exception) {
            Log.e("KeySyncRepository", "Failed to upsert keys: ${e.message}", e)
            throw Exception("Échec de la synchronisation des clés: ${e.message}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) return

        val sinceEpoch = keyDao.getMaxServerUpdatedAtOrNull()
        val sinceIso = sinceEpoch?.let { Instant.ofEpochSecond(it).toString() }

        try {
            val response = supabase.from("keys").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                    if (sinceIso != null) {
                        filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                    }
                }
            }

            val rows = response.decodeList<KeyRow>()
            if (rows.isEmpty()) return

            val entities = rows.mapNotNull { row ->
                val housing = housingDao.getByRemoteId(row.housingRemoteId)
                if (housing == null) return@mapNotNull null

                val existing = keyDao.getByRemoteId(row.remoteId)
                row.toEntityPreservingLocalId(
                    localId = existing?.id ?: 0L,
                    housingLocalId = housing.id
                )
            }

            if (entities.isEmpty()) return

            keyDao.upsertAll(entities)
            entities.forEach { e ->
                val serverUpdated = e.serverUpdatedAtEpochSeconds
                if (serverUpdated != null) {
                    keyDao.markClean(e.remoteId, serverUpdated)
                }
            }

            Log.d("KeySyncRepository", "Successfully pulled and saved ${entities.size} records")
        } catch (e: Exception) {
            Log.e("KeySyncRepository", "Failed to pull updates: ${e.message}", e)
            throw e
        }
    }
}

// ============================================================================
// INDEXATION EVENT SYNC REPOSITORY
// ============================================================================

class IndexationEventSyncRepository(
    private val supabase: SupabaseClient,
    private val indexationEventDao: IndexationEventDao,
    private val leaseDao: LeaseDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        try {
            pushDirty()
            pullUpdates()
        } catch (e: Exception) {
            Log.e("IndexationEventSyncRepository", "Sync failed: ${e.message}", e)
            throw e
        }
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            Log.w("IndexationEventSyncRepository", "User not authenticated - skipping push")
            return
        }

        val dirty = indexationEventDao.getDirty()
        if (dirty.isEmpty()) {
            Log.d("IndexationEventSyncRepository", "No dirty records to push")
            return
        }

        val payload = dirty.mapNotNull { entity ->
            val lease = leaseDao.getById(entity.leaseId)
            if (lease == null) {
                Log.e("IndexationEventSyncRepository", "Missing lease for event ${entity.id}")
                return@mapNotNull null
            }

            entity.toRow(userId = user.id, leaseRemoteId = lease.remoteId)
        }

        if (payload.isEmpty()) return

        try {
            supabase.from("indexation_events").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            Log.d("IndexationEventSyncRepository", "Successfully pushed ${payload.size} records")
        } catch (e: Exception) {
            Log.e("IndexationEventSyncRepository", "Failed to upsert events: ${e.message}", e)
            throw Exception("Échec de la synchronisation des indexations: ${e.message}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) return

        val sinceEpoch = indexationEventDao.getMaxServerUpdatedAtOrNull()
        val sinceIso = sinceEpoch?.let { Instant.ofEpochSecond(it).toString() }

        try {
            val response = supabase.from("indexation_events").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                    if (sinceIso != null) {
                        filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                    }
                }
            }

            val rows = response.decodeList<IndexationEventRow>()
            if (rows.isEmpty()) return

            val entities = rows.mapNotNull { row ->
                val lease = leaseDao.getByRemoteId(row.leaseRemoteId)
                if (lease == null) return@mapNotNull null

                val existing = indexationEventDao.getByRemoteId(row.remoteId)
                row.toEntityPreservingLocalId(
                    localId = existing?.id ?: 0L,
                    leaseLocalId = lease.id
                )
            }

            if (entities.isEmpty()) return

            indexationEventDao.upsertAll(entities)
            entities.forEach { e ->
                val serverUpdated = e.serverUpdatedAtEpochSeconds
                if (serverUpdated != null) {
                    indexationEventDao.markClean(e.remoteId, serverUpdated)
                }
            }

            Log.d("IndexationEventSyncRepository", "Successfully pulled and saved ${entities.size} records")
        } catch (e: Exception) {
            Log.e("IndexationEventSyncRepository", "Failed to pull updates: ${e.message}", e)
            throw e
        }
    }
}