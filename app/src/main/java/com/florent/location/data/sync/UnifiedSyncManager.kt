package com.florent.location.ui.sync

import android.util.Log
import com.florent.location.data.sync.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Gestionnaire de synchronisation unifié pour toutes les entités
 *
 * ORDRE DE SYNCHRONISATION IMPORTANT :
 * 1. Tenants (pas de dépendances)
 * 2. Housings (pas de dépendances)
 * 3. Leases (dépend de Tenants et Housings)
 * 4. Keys (dépend de Housings)
 * 5. IndexationEvents (dépend de Leases)
 */
class UnifiedSyncManager(
    private val housingRepo: HousingSyncRepository,
    private val tenantRepo: TenantSyncRepository,
    private val leaseRepo: LeaseSyncRepository,
    private val keyRepo: KeySyncRepository,
    private val indexationEventRepo: IndexationEventSyncRepository,
    private val supabase: SupabaseClient,
    private val scope: CoroutineScope
) : HousingSyncRequester {

    private val _syncChannel = Channel<String>(Channel.CONFLATED)
    private var pendingSyncJob: Job? = null

    init {
        scope.launch {
            for (reason in _syncChannel) {
                Log.d("UnifiedSyncManager", "Sync triggered: $reason")
                try {
                    syncAll()
                    Log.d("UnifiedSyncManager", "Sync completed successfully")
                } catch (e: Exception) {
                    Log.e("UnifiedSyncManager", "Sync failed: ${e.message}", e)
                }
            }
        }
    }


    suspend fun getCurrentUser(): io.github.jan.supabase.auth.user.UserInfo? {
        return supabase.auth.currentUserOrNull()
    }

    /**
     * Synchronise toutes les entités dans le bon ordre
     */
    private suspend fun syncAll() {
        Log.d("UnifiedSyncManager", "Starting full sync...")

        // Étape 1 : Synchroniser les entités sans dépendances
        try {
            Log.d("UnifiedSyncManager", "Syncing tenants...")
            tenantRepo.syncOnce()
            Log.d("UnifiedSyncManager", "Tenants synced successfully")
        } catch (e: Exception) {
            Log.e("UnifiedSyncManager", "Tenant sync failed: ${e.message}", e)
            // Continue quand même avec les autres
        }

        try {
            Log.d("UnifiedSyncManager", "Syncing housings...")
            housingRepo.syncOnce()
            Log.d("UnifiedSyncManager", "Housings synced successfully")
        } catch (e: Exception) {
            Log.e("UnifiedSyncManager", "Housing sync failed: ${e.message}", e)
        }

        // Étape 2 : Synchroniser les leases (dépend de tenants + housings)
        try {
            Log.d("UnifiedSyncManager", "Syncing leases...")
            leaseRepo.syncOnce()
            Log.d("UnifiedSyncManager", "Leases synced successfully")
        } catch (e: Exception) {
            Log.e("UnifiedSyncManager", "Lease sync failed: ${e.message}", e)
        }

        // Étape 3 : Synchroniser les keys (dépend de housings)
        try {
            Log.d("UnifiedSyncManager", "Syncing keys...")
            keyRepo.syncOnce()
            Log.d("UnifiedSyncManager", "Keys synced successfully")
        } catch (e: Exception) {
            Log.e("UnifiedSyncManager", "Key sync failed: ${e.message}", e)
        }

        // Étape 4 : Synchroniser les indexation events (dépend de leases)
        try {
            Log.d("UnifiedSyncManager", "Syncing indexation events...")
            indexationEventRepo.syncOnce()
            Log.d("UnifiedSyncManager", "Indexation events synced successfully")
        } catch (e: Exception) {
            Log.e("UnifiedSyncManager", "Indexation event sync failed: ${e.message}", e)
        }

        Log.d("UnifiedSyncManager", "Full sync completed")
    }

    /**
     * Synchronise seulement une entité spécifique
     * Utile pour optimiser les syncs après une modification locale
     */
    suspend fun syncEntity(entity: SyncEntity) {
        when (entity) {
            SyncEntity.TENANT -> tenantRepo.syncOnce()
            SyncEntity.HOUSING -> housingRepo.syncOnce()
            SyncEntity.LEASE -> {
                // Leases nécessitent que tenants et housings soient à jour
                tenantRepo.syncOnce()
                housingRepo.syncOnce()
                leaseRepo.syncOnce()
            }
            SyncEntity.KEY -> {
                // Keys nécessitent que housings soient à jour
                housingRepo.syncOnce()
                keyRepo.syncOnce()
            }
            SyncEntity.INDEXATION_EVENT -> {
                // Indexation events nécessitent que leases (et donc tenants + housings) soient à jour
                tenantRepo.syncOnce()
                housingRepo.syncOnce()
                leaseRepo.syncOnce()
                indexationEventRepo.syncOnce()
            }
        }
    }

    override fun requestSync(reason: String, debounceMs: Long) {
        pendingSyncJob?.cancel()
        pendingSyncJob = scope.launch {
            val effectiveDebounceMs = debounceMs.coerceAtLeast(0)
            if (effectiveDebounceMs > 0) {
                delay(effectiveDebounceMs)
            }
            _syncChannel.trySend(reason)
        }
    }
}

enum class SyncEntity {
    TENANT,
    HOUSING,
    LEASE,
    KEY,
    INDEXATION_EVENT
}