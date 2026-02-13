package com.florent.location.data.sync

import com.florent.location.data.db.dao.HousingDao
import com.florent.location.data.db.dao.LeaseDao
import com.florent.location.data.db.dao.TenantDao
import com.florent.location.data.db.entity.AddressEntity
import com.florent.location.data.db.entity.HousingEntity
import com.florent.location.data.db.entity.LeaseEntity
import com.florent.location.data.db.entity.TenantEntity
import com.florent.location.domain.model.PebRating
import io.github.jan.supabase.SupabaseClient
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncRepositoryDeleteFailureTest {

    @Test
    fun housingDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val housingDao = mockk<HousingDao>(relaxed = true)
        val repository = HousingSyncRepository(supabase, housingDao)
        val entity = HousingEntity(
            id = 12L,
            remoteId = "remote-housing",
            address = AddressEntity(number = "1", street = "Rue Test", zipCode = "1000", city = "Bruxelles"),
            isDeleted = true,
            dirty = true,
            pebRating = PebRating.UNKNOWN
        )

        repository.deleteDeletedHousing(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        coVerify(exactly = 0) { housingDao.deleteById(any()) }
    }

    @Test
    fun tenantDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val tenantDao = mockk<TenantDao>(relaxed = true)
        val repository = TenantSyncRepository(supabase, tenantDao)
        val entity = TenantEntity(
            id = 24L,
            remoteId = "remote-tenant",
            firstName = "Jean",
            lastName = "Dupont",
            isDeleted = true,
            dirty = true
        )

        repository.deleteDeletedTenant(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        coVerify(exactly = 0) { tenantDao.deleteById(any()) }
    }

    @Test
    fun leaseDelete_keepsLocalEntity_whenRemoteDeleteFails() = runTest {
        val supabase = mockk<SupabaseClient>(relaxed = true)
        val leaseDao = mockk<LeaseDao>(relaxed = true)
        val housingDao = mockk<HousingDao>(relaxed = true)
        val tenantDao = mockk<TenantDao>(relaxed = true)
        val repository = LeaseSyncRepository(supabase, leaseDao, housingDao, tenantDao)
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

        repository.deleteDeletedLease(entity, "user-1") {
            throw RuntimeException("Supabase delete failed")
        }

        coVerify(exactly = 0) { leaseDao.hardDeleteByRemoteId(any()) }
    }
}
