package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.LeaseDao
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
        pushDirty()
        pullUpdates()
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = leaseDao.getDirty()
        if (dirty.isEmpty()) return

        dirty.filter { it.isDeleted }.forEach { entity ->
            deleteDeletedLease(entity, user.id)
        }

        val payload = dirty.filterNot { it.isDeleted }.mapNotNull { entity ->
            val housing = housingDao.getById(entity.housingId)
            val tenant = tenantDao.getById(entity.tenantId)
            if (housing == null || tenant == null) {
                Log.w("LeaseSyncRepository", "Missing relation for lease ${entity.id}")
                null
            } else entity.toRow(user.id, housing.remoteId, tenant.remoteId)
        }

        if (payload.isNotEmpty()) {
            supabase.from("leases").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
        }
    }


    internal suspend fun deleteDeletedLease(entity: LeaseEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("leases").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }) {
        try {
            remoteDelete()
            leaseDao.hardDeleteByRemoteId(entity.remoteId)
        } catch (e: Exception) {
            Log.e("LeaseSyncRepository", "Failed to delete remote lease ${entity.remoteId}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceIso = leaseDao.getMaxServerUpdatedAtOrNull()?.let { Instant.ofEpochSecond(it).toString() }

        val rows = supabase.from("leases").select {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
            }
        }.decodeList<LeaseRow>()

        val entities = rows.mapNotNull { row ->
            val housing = housingDao.getByRemoteId(row.housingRemoteId)
            val tenant = tenantDao.getByRemoteId(row.tenantRemoteId)
            if (housing == null || tenant == null) null
            else {
                val existing = leaseDao.getByRemoteId(row.remoteId)
                row.toEntityPreservingLocalId(existing?.id ?: 0L, housing.id, tenant.id, existing?.createdAt)
            }
        }
        if (entities.isNotEmpty()) {
            leaseDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochSeconds?.let { leaseDao.markClean(e.remoteId, it) } }
        }

        val remoteIds = supabase.from("leases").select {
            filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
        }.decodeList<LeaseRow>().map { it.remoteId }.toSet()

        leaseDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach { leaseDao.hardDeleteByRemoteId(it) }
    }
}
