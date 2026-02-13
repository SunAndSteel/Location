package com.florent.location.data.sync

import com.florent.location.presentation.sync.SyncState
import io.github.jan.supabase.SupabaseClient
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnifiedSyncManagerDeleteFailureTest {

    @Test
    fun requestSync_setsGlobalStateToError_whenDeleteFailsInRepository() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val managerScope = CoroutineScope(SupervisorJob() + dispatcher)
        val housingRepo = mockk<HousingSyncRepository>(relaxed = true)
        val tenantRepo = mockk<TenantSyncRepository>(relaxed = true)
        val leaseRepo = mockk<LeaseSyncRepository>(relaxed = true)
        val keyRepo = mockk<KeySyncRepository>(relaxed = true)
        val indexationEventRepo = mockk<IndexationEventSyncRepository>(relaxed = true)
        val supabase = mockk<SupabaseClient>(relaxed = true)

        coEvery { tenantRepo.syncOnce() } throws SyncDeleteFailuresException(
            listOf(SyncDeleteResult.Failure("Tenant", "tenant-42", "Supabase delete failed"))
        )

        val manager = UnifiedSyncManager(
            housingRepo = housingRepo,
            tenantRepo = tenantRepo,
            leaseRepo = leaseRepo,
            keyRepo = keyRepo,
            indexationEventRepo = indexationEventRepo,
            supabase = supabase,
            scope = managerScope
        )

        manager.requestSync("test", debounceMs = 0)
        advanceUntilIdle()

        assertTrue(manager.state.value is SyncState.Error)
        val error = manager.state.value as SyncState.Error
        assertTrue(error.message.contains("Tenant sync delete failed for Tenant(tenant-42): Supabase delete failed"))

        managerScope.cancel()
    }
}
