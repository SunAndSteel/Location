package com.florent.location.domain.usecase.housing

import com.florent.location.fake.FakeHousingRepository
import com.florent.location.sampleHousing
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HousingUseCasesTest {

    @Test
    fun `createHousing inserts housing and returns id`() = runTest {
        val repository = FakeHousingRepository()
        val useCases = HousingUseCasesImpl(repository)

        val id = useCases.createHousing(
            sampleHousing(
                city = "Bruxelles",
                rentCents = 90000,
                chargesCents = 15000,
                depositCents = 180000
            )
        )

        assertEquals(1L, id)
        val housings = repository.observeHousings().first()
        assertEquals(1, housings.size)
    }

    @Test
    fun `deleteHousing removes housing when no active lease exists`() = runTest {
        val housing = sampleHousing(
            id = 1L,
            city = "Bruxelles",
            rentCents = 90000,
            chargesCents = 15000,
            depositCents = 180000
        )
        val repository = FakeHousingRepository(listOf(housing))
        val useCases = HousingUseCasesImpl(repository)

        useCases.deleteHousing(housing.id)

        val housings = repository.observeHousings().first()
        assertTrue(housings.isEmpty())
    }

    @Test
    fun `deleteHousing fails when active lease exists`() = runTest {
        val housing = sampleHousing(
            id = 1L,
            city = "Bruxelles",
            rentCents = 90000,
            chargesCents = 15000,
            depositCents = 180000
        )
        val repository = FakeHousingRepository(listOf(housing))
        repository.setActiveLease(housing.id, true)
        val useCases = HousingUseCasesImpl(repository)

        try {
            useCases.deleteHousing(housing.id)
            throw AssertionError("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals(
                "Suppression impossible: un bail actif existe pour ce logement.",
                error.message
            )
        }
    }
}
