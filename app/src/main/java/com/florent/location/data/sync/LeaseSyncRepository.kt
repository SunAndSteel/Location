package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.TenantDao
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

class LeaseSyncRepository(
    private val supabase: SupabaseClient,
    private val leaseDao: LeaseDao,
    private val housingDao: HousingDao,
    private val tenantDao: TenantDao
) {
    private val mutex = Mutex()

    suspend fun syncOnce() = mutex.withLock {
        try {
            pushDirty()
            pullUpdates()
        } catch (e: Exception) {
            Log.e("LeaseSyncRepository", "Sync failed: ${e.message}", e)
            throw e
        }
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            Log.w("LeaseSyncRepository", "User not authenticated - skipping push")
            return
        }

        val dirty = leaseDao.getDirty()
        if (dirty.isEmpty()) {
            Log.d("LeaseSyncRepository", "No dirty records to push")
            return
        }

        Log.d("LeaseSyncRepository", "Pushing ${dirty.size} dirty records")

        val payload = dirty.mapNotNull { entity ->
            // Récupérer les remote_id du housing et tenant
            val housing = housingDao.getById(entity.housingId)
            val tenant = tenantDao.getById(entity.tenantId)

            if (housing == null || tenant == null) {
                Log.e("LeaseSyncRepository", "Missing housing or tenant for lease ${entity.id}")
                return@mapNotNull null
            }

            val row = entity.toRow(
                userId = user.id,
                housingRemoteId = housing.remoteId,
                tenantRemoteId = tenant.remoteId
            )
            Log.d("LeaseSyncRepository", "Upserting lease with remote_id: ${row.remoteId}")
            row
        }

        if (payload.isEmpty()) {
            Log.w("LeaseSyncRepository", "No valid leases to push (missing relations)")
            return
        }

        try {
            supabase.from("leases").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
            Log.d("LeaseSyncRepository", "Successfully pushed ${payload.size} records")
        } catch (e: Exception) {
            Log.e("LeaseSyncRepository", "Failed to upsert leases: ${e.message}", e)
            throw Exception("Échec de la synchronisation des baux: ${e.message}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull()
        if (user == null) {
            Log.w("LeaseSyncRepository", "User not authenticated - skipping pull")
            return
        }

        val sinceEpoch = leaseDao.getMaxServerUpdatedAtOrNull()
        val sinceIso = sinceEpoch?.let { Instant.ofEpochSecond(it).toString() }

        Log.d("LeaseSyncRepository", "Pulling updates since: $sinceIso")

        try {
            val response = supabase.from("leases").select {
                filter {
                    filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)

                    if (sinceIso != null) {
                        filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
                    }
                }
            }

            val rows = response.decodeList<LeaseRow>()
            Log.d("LeaseSyncRepository", "Pulled ${rows.size} records")

            if (rows.isEmpty()) return

            val entities = rows.mapNotNull { row ->
                // Convertir les remote_id en local ID
                val housing = housingDao.getByRemoteId(row.housingRemoteId)
                val tenant = tenantDao.getByRemoteId(row.tenantRemoteId)

                if (housing == null || tenant == null) {
                    Log.w("LeaseSyncRepository", "Missing local housing or tenant for lease ${row.remoteId}")
                    return@mapNotNull null
                }

                val existing = leaseDao.getByRemoteId(row.remoteId)
                row.toEntityPreservingLocalId(
                    localId = existing?.id ?: 0L,
                    housingLocalId = housing.id,
                    tenantLocalId = tenant.id
                )
            }

            if (entities.isEmpty()) {
                Log.w("LeaseSyncRepository", "No valid leases to save (missing local relations)")
                return
            }

            leaseDao.upsertAll(entities)

            entities.forEach { e ->
                val serverUpdated = e.serverUpdatedAtEpochSeconds
                if (serverUpdated != null) {
                    leaseDao.markClean(e.remoteId, serverUpdated)
                }
            }

            Log.d("LeaseSyncRepository", "Successfully pulled and saved ${entities.size} records")
        } catch (e: Exception) {
            Log.e("LeaseSyncRepository", "Failed to pull updates: ${e.message}", e)
            throw Exception("Échec du téléchargement des baux: ${e.message}", e)
        }
    }
}