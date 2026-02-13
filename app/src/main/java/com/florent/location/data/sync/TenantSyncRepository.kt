package com.florent.location.data.sync

import android.util.Log
import com.florent.location.data.db.entity.TenantEntity
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
        pushDirty()
        pullUpdates()
    }

    private suspend fun pushDirty() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val dirty = tenantDao.getDirty()
        if (dirty.isEmpty()) return

        val deleted = dirty.filter { it.isDeleted }
        deleted.forEach { entity ->
            deleteDeletedTenant(entity, user.id)
        }

        val payload = dirty.filterNot { it.isDeleted }.map { it.toRow(userId = user.id) }
        if (payload.isNotEmpty()) {
            supabase.from("tenants").upsert(payload) {
                onConflict = "remote_id"
                ignoreDuplicates = false
            }
        }
    }


    internal suspend fun deleteDeletedTenant(entity: TenantEntity, userId: String, remoteDelete: suspend () -> Unit = {
        supabase.from("tenants").delete {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = userId)
                filter(column = "remote_id", operator = FilterOperator.EQ, value = entity.remoteId)
            }
        }
    }) {
        try {
            remoteDelete()
            tenantDao.deleteById(entity.id)
        } catch (e: Exception) {
            Log.e("TenantSyncRepository", "Failed to delete remote tenant ${entity.remoteId}", e)
        }
    }

    private suspend fun pullUpdates() {
        val user = supabase.auth.currentUserOrNull() ?: return
        val sinceEpoch = tenantDao.getMaxServerUpdatedAtOrNull()
        val sinceIso = sinceEpoch?.let { Instant.ofEpochSecond(it).toString() }

        val rows = supabase.from("tenants").select {
            filter {
                filter(column = "user_id", operator = FilterOperator.EQ, value = user.id)
                if (sinceIso != null) filter(column = "updated_at", operator = FilterOperator.GT, value = sinceIso)
            }
        }.decodeList<TenantRow>()

        val entities = rows.map { row ->
            val existing = tenantDao.getByRemoteId(row.remoteId)
            row.toEntityPreservingLocalId(localId = existing?.id ?: 0L, existingCreatedAtMillis = existing?.createdAt)
        }
        if (entities.isNotEmpty()) {
            tenantDao.upsertAll(entities)
            entities.forEach { e -> e.serverUpdatedAtEpochSeconds?.let { tenantDao.markClean(e.remoteId, it) } }
        }

        val remoteIds = supabase.from("tenants").select {
            filter { filter(column = "user_id", operator = FilterOperator.EQ, value = user.id) }
        }.decodeList<TenantRow>().map { it.remoteId }.toSet()

        tenantDao.getAllRemoteIds().filterNot { remoteIds.contains(it) }.forEach { tenantDao.hardDeleteByRemoteId(it) }
    }
}
