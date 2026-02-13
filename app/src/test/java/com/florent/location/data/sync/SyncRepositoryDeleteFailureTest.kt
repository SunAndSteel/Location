package com.florent.location.data.sync

import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.IndexationEventDao
import com.florent.location.data.db.dao.KeyDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.data.db.dao.TenantDao
import com.florent.location.data.db.dao.SyncCursorDao
import com.florent.location.data.db.entity.AddressEntity
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.data.db.entity.IndexationEventEntity
import com.florent.location.data.db.entity.KeyEntity
import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.data.db.entity.TenantEntity
import com.florent.location.domain.model.PebRating
import io.github.jan.supabase.SupabaseClient
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncRepositoryDeleteFailureTest {

    @Test
    fun housingDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val housingDao = mockk<HousingDao>(relaxed = true)
        val syncCursorDao = mockk<SyncCursorDao>(relaxed = true)
        val repository = HousingSyncRepository(supabase, housingDao, syncCursorDao)
        val entity = HousingEntity(
            id = 12L,
            remoteId = "remote-housing",
            address = AddressEntity(number = "1", street = "Rue Test", zipCode = "1000", city = "Bruxelles"),
            isDeleted = true,
            dirty = true,
            pebRating = PebRating.UNKNOWN
        )

        val result = repository.deleteDeletedHousing(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        assertTrue(result is SyncDeleteResult.Failure)
        coVerify(exactly = 0) { housingDao.deleteById(any()) }
    }

    @Test
    fun tenantDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val tenantDao = mockk<TenantDao>(relaxed = true)
        val syncCursorDao = mockk<SyncCursorDao>(relaxed = true)
        val repository = TenantSyncRepository(supabase, tenantDao, syncCursorDao)
        val entity = TenantEntity(
            id = 24L,
            remoteId = "remote-tenant",
            firstName = "Jean",
            lastName = "Dupont",
            isDeleted = true,
            dirty = true
        )

        val result = repository.deleteDeletedTenant(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        assertTrue(result is SyncDeleteResult.Failure)
        coVerify(exactly = 0) { tenantDao.deleteById(any()) }
    }

    @Test
    fun leaseDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val leaseDao = mockk<LeaseDao>(relaxed = true)
        val housingDao = mockk<HousingDao>(relaxed = true)
        val tenantDao = mockk<TenantDao>(relaxed = true)
        val syncCursorDao = mockk<SyncCursorDao>(relaxed = true)
        val repository = LeaseSyncRepository(supabase, leaseDao, housingDao, tenantDao, syncCursorDao)
        val entity = LeaseEntity(
            id = 99L,
            remoteId = "remote-lease",
            housingId = 1L,
            tenantId = 2L,
            startDateEpochDay = 20000,
            rentCents = 100000,
            chargesCents = 10000,
            isDeleted = true,
            dirty = true
        )

        val result = repository.deleteDeletedLease(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        assertTrue(result is SyncDeleteResult.Failure)
        coVerify(exactly = 0) { leaseDao.hardDeleteByRemoteId(any()) }
    }

    @Test
    fun keyDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val keyDao = mockk<KeyDao>(relaxed = true)
        val housingDao = mockk<HousingDao>(relaxed = true)
        val syncCursorDao = mockk<SyncCursorDao>(relaxed = true)
        val repository = KeySyncRepository(supabase, keyDao, housingDao, syncCursorDao)
        val entity = KeyEntity(
            id = 34L,
            remoteId = "remote-key",
            housingId = 1L,
            type = "Maison",
            handedOverEpochDay = 20000,
            isDeleted = true,
            dirty = true
        )

        val result = repository.deleteDeletedKey(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        assertTrue(result is SyncDeleteResult.Failure)
        coVerify(exactly = 0) { keyDao.deleteById(any()) }
    }

    @Test
    fun indexationEventDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val indexationEventDao = mockk<IndexationEventDao>(relaxed = true)
        val leaseDao = mockk<LeaseDao>(relaxed = true)
        val syncCursorDao = mockk<SyncCursorDao>(relaxed = true)
        val repository = IndexationEventSyncRepository(supabase, indexationEventDao, leaseDao, syncCursorDao)
        val entity = IndexationEventEntity(
            id = 56L,
            remoteId = "remote-event",
            leaseId = 1L,
            appliedEpochDay = 20000,
            baseRentCents = 100000,
            indexPercent = 2.3,
            newRentCents = 102300,
            isDeleted = true,
            dirty = true
        )

        val result = repository.deleteDeletedIndexationEvent(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        assertTrue(result is SyncDeleteResult.Failure)
        coVerify(exactly = 0) { indexationEventDao.hardDeleteByRemoteId(any()) }
    }
}
