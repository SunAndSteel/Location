package com.florent.location.domain.usecase.lease

import com.florent.location.domain.model.Key
import com.florent.location.fake.FakeLeaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeaseUseCasesTest {

    @Test
    fun `createLease fails if housing does not exist`() = runTest {
        val repository = FakeLeaseRepository(existingTenantIds = setOf(1L))
        val useCases = LeaseUseCasesImpl(repository)
        val request = LeaseCreateRequest(
            housingId = 42L,
            tenantId = 1L,
            startDateEpochDay = 1000L,
            rentCents = 100000L,
            chargesCents = 10000L,
            depositCents = 50000L,
            rentDueDayOfMonth = 5,
            mailboxLabel = null,
            meterGas = "G",
            meterElectricity = "E",
            meterWater = "W",
            keys = emptyList()
        )

        val error = runCatching { useCases.createLease(request) }.exceptionOrNull()
        assertEquals("Le logement sélectionné n'existe pas.", error?.message)
    }

    @Test
    fun `createLease fails if tenant does not exist`() = runTest {
        val repository = FakeLeaseRepository(existingHousingIds = setOf(1L))
        val useCases = LeaseUseCasesImpl(repository)
        val request = LeaseCreateRequest(
            housingId = 1L,
            tenantId = 9L,
            startDateEpochDay = 1000L,
            rentCents = 100000L,
            chargesCents = 10000L,
            depositCents = 50000L,
            rentDueDayOfMonth = 5,
            mailboxLabel = null,
            meterGas = null,
            meterElectricity = null,
            meterWater = null,
            keys = emptyList()
        )

        val error = runCatching { useCases.createLease(request) }.exceptionOrNull()
        assertEquals("Le locataire sélectionné n'existe pas.", error?.message)
    }

    @Test
    fun `createLease fails if rentDueDayOfMonth not in range`() = runTest {
        val repository = FakeLeaseRepository(existingHousingIds = setOf(1L), existingTenantIds = setOf(2L))
        val useCases = LeaseUseCasesImpl(repository)
        val request = LeaseCreateRequest(
            housingId = 1L,
            tenantId = 2L,
            startDateEpochDay = 1000L,
            rentCents = 100000L,
            chargesCents = 10000L,
            depositCents = 50000L,
            rentDueDayOfMonth = 31,
            mailboxLabel = null,
            meterGas = null,
            meterElectricity = null,
            meterWater = null,
            keys = emptyList()
        )

        val error = runCatching { useCases.createLease(request) }.exceptionOrNull()
        assertEquals("Le jour d'échéance doit être entre 1 et 28.", error?.message)
    }

    @Test
    fun `createLease fails if active lease exists for housing`() = runTest {
        val repository = FakeLeaseRepository(existingHousingIds = setOf(1L), existingTenantIds = setOf(2L))
        val useCases = LeaseUseCasesImpl(repository)
        val request = LeaseCreateRequest(
            housingId = 1L,
            tenantId = 2L,
            startDateEpochDay = 1000L,
            rentCents = 100000L,
            chargesCents = 10000L,
            depositCents = 50000L,
            rentDueDayOfMonth = 5,
            mailboxLabel = null,
            meterGas = null,
            meterElectricity = null,
            meterWater = null,
            keys = emptyList()
        )

        useCases.createLease(request)

        val error = runCatching { useCases.createLease(request) }.exceptionOrNull()
        assertEquals("Un bail actif existe déjà pour ce logement.", error?.message)
    }

    @Test
    fun `createLease succeeds and persists meters and keys`() = runTest {
        val repository = FakeLeaseRepository(existingHousingIds = setOf(1L), existingTenantIds = setOf(2L))
        val useCases = LeaseUseCasesImpl(repository)
        val keys = listOf(
            Key(type = "Badge", deviceLabel = "Hall", handedOverEpochDay = 1001L),
            Key(type = "Clé", deviceLabel = null, handedOverEpochDay = 1002L)
        )
        val request = LeaseCreateRequest(
            housingId = 1L,
            tenantId = 2L,
            startDateEpochDay = 1000L,
            rentCents = 100000L,
            chargesCents = 10000L,
            depositCents = 50000L,
            rentDueDayOfMonth = 5,
            mailboxLabel = "Boite A",
            meterGas = "G1",
            meterElectricity = "E1",
            meterWater = "W1",
            keys = keys
        )

        val leaseId = useCases.createLease(request)

        val storedLease = useCases.observeLease(leaseId).first()
        assertNotNull(storedLease)
        assertEquals("G1", storedLease?.meterGas)
        assertEquals("E1", storedLease?.meterElectricity)
        assertEquals("W1", storedLease?.meterWater)

        val storedKeys = useCases.observeKeysForLease(leaseId).first()
        assertEquals(2, storedKeys.size)
        assertEquals("Badge", storedKeys[0].type)
        assertEquals("Clé", storedKeys[1].type)
    }
}
