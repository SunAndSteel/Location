package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.dao.TenantDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

class TenantSyncRepository(
    private val supabase: SupabaseClient,
    private val tenantDao: TenantDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        try {
            pushDirty()
            pullUpdates()
        } catch (e: Exception) {
            Log.e("TenantSyncRepository", "Sync failed: ${e.message}", e)
            throw e
        }
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            Log.w("TenantSyncRepository", "User not authenticated - skipping push")
            return
        }

        Log.d("TenantSyncRepository", "Authenticated user ID: ${user.id}")

        val dirty = tenantDao.getDirty()
        if (dirty.isEmpty()) {
            Log.d("TenantSyncRepository", "No dirty records to push")
            return
        }

        Log.d("TenantSyncRepository", "Pushing ${dirty.size} dirty records")

        val payload = dirty.map { entity ->
            val row = entity.toRow(userId = user.id)
            Log.d("TenantSyncRepository", "Upserting tenant with remote_id: ${row.remoteId}, user_id: ${row.userId}")
            row
        }

        try {
            supabase.from("tenants").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            Log.d("TenantSyncRepository", "Successfully pushed ${payload.size} records")
        } catch (e: Exception) {
            Log.e("TenantSyncRepository", "Failed to upsert tenants: ${e.message}", e)
            throw Exception("Échec de la synchronisation des locataires: ${e.message}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            Log.w("TenantSyncRepository", "User not authenticated - skipping pull")
            return
        }

        val sinceEpoch = tenantDao.getMaxServerUpdatedAtOrNull()
        val sinceIso = sinceEpoch?.let { Instant.ofEpochSecond(it).toString() }

        Log.d("TenantSyncRepository", "Pulling updates since: $sinceIso")

        try {
            val response = supabase.from("tenants").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)

                    if (sinceIso != null) {
                        filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                    }
                }
            }

            val rows = response.decodeList<TenantRow>()
            Log.d("TenantSyncRepository", "Pulled ${rows.size} records")

            if (rows.isEmpty()) return

            val entities = rows.map { row ->
                val existing = tenantDao.getByRemoteId(row.remoteId)
                row.toEntityPreservingLocalId(
                    localId = existing?.id ?: 0L,
                    existingCreatedAtMillis = existing?.createdAt
                )
            }

            tenantDao.upsertAll(entities)

            entities.forEach { e ->
                val serverUpdated = e.serverUpdatedAtEpochSeconds
                if (serverUpdated != null) {
                    tenantDao.markClean(e.remoteId, serverUpdated)
                }
            }

            Log.d("TenantSyncRepository", "Successfully pulled and saved ${entities.size} records")
        } catch (e: Exception) {
            Log.e("TenantSyncRepository", "Failed to pull updates: ${e.message}", e)
            throw Exception("Échec du téléchargement des locataires: ${e.message}", e)
        }
    }
}