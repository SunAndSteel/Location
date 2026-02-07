package com.florent.location.domain.usecase.indexation

import com.florent.location.domain.model.Lease
import com.florent.location.fake.FakeLeaseRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IndexationUseCasesTest {

    @Test
    fun `future anniversary in same year uses anniversary date`() = runTest {
        val today = LocalDate.of(2024, 6, 1)
        val anniversary = LocalDate.of(2024, 12, 1)
        val lease = leaseFor(
            id = 1L,
            anniversary = anniversary.toEpochDay()
        )
        val useCases = IndexationUseCasesImpl(FakeLeaseRepository(leases = listOf(lease)))

        val result = useCases.observeUpcomingIndexations(today.toEpochDay()).first()

        assertEquals(1, result.size)
        val item = result.first()
        assertEquals(anniversary.toEpochDay(), item.nextIndexationEpochDay)
        assertEquals(
            anniversary.toEpochDay() - today.toEpochDay(),
            item.daysUntil.toLong()
        )
    }

    @Test
    fun `past anniversary shifts to next year`() = runTest {
        val today = LocalDate.of(2024, 6, 1)
        val anniversary = LocalDate.of(2024, 1, 1)
        val expectedNext = LocalDate.of(2025, 1, 1)
        val lease = leaseFor(
            id = 2L,
            anniversary = anniversary.toEpochDay()
        )
        val useCases = IndexationUseCasesImpl(FakeLeaseRepository(leases = listOf(lease)))

        val result = useCases.observeUpcomingIndexations(today.toEpochDay()).first()

        assertEquals(1, result.size)
        val item = result.first()
        assertEquals(expectedNext.toEpochDay(), item.nextIndexationEpochDay)
        assertEquals(
            expectedNext.toEpochDay() - today.toEpochDay(),
            item.daysUntil.toLong()
        )
    }

    @Test
    fun `null index anniversary uses start date`() = runTest {
        val today = LocalDate.of(2024, 6, 1)
        val startDate = LocalDate.of(2023, 5, 1)
        val expectedNext = LocalDate.of(2025, 5, 1)
        val lease = leaseFor(
            id = 3L,
            anniversary = null,
            startDate = startDate.toEpochDay()
        )
        val useCases = IndexationUseCasesImpl(FakeLeaseRepository(leases = listOf(lease)))

        val result = useCases.observeUpcomingIndexations(today.toEpochDay()).first()

        assertEquals(1, result.size)
        val item = result.first()
        assertEquals(expectedNext.toEpochDay(), item.nextIndexationEpochDay)
        assertEquals(
            expectedNext.toEpochDay() - today.toEpochDay(),
            item.daysUntil.toLong()
        )
    }

    @Test
    fun `results are sorted by days until ascending`() = runTest {
        val today = LocalDate.of(2024, 6, 1)
        val earlier = LocalDate.of(2024, 7, 1)
        val later = LocalDate.of(2024, 11, 1)
        val leases = listOf(
            leaseFor(id = 10L, anniversary = later.toEpochDay()),
            leaseFor(id = 11L, anniversary = earlier.toEpochDay())
        )
        val useCases = IndexationUseCasesImpl(FakeLeaseRepository(leases = leases))

        val result = useCases.observeUpcomingIndexations(today.toEpochDay()).first()

        assertEquals(listOf(11L, 10L), result.map { it.leaseId })
        assertTrue(result.zipWithNext().all { (a, b) -> a.daysUntil <= b.daysUntil })
    }

    @Test
    fun `only active leases are included`() = runTest {
        val today = LocalDate.of(2024, 6, 1)
        val activeLease = leaseFor(
            id = 20L,
            anniversary = LocalDate.of(2024, 8, 1).toEpochDay()
        )
        val inactiveLease = leaseFor(
            id = 21L,
            anniversary = LocalDate.of(2024, 9, 1).toEpochDay(),
            endDate = LocalDate.of(2024, 5, 1).toEpochDay()
        )
        val useCases = IndexationUseCasesImpl(
            FakeLeaseRepository(leases = listOf(activeLease, inactiveLease))
        )

        val result = useCases.observeUpcomingIndexations(today.toEpochDay()).first()

        assertEquals(listOf(20L), result.map { it.leaseId })
    }

    private fun leaseFor(
        id: Long,
        anniversary: Long?,
        startDate: Long = LocalDate.of(2023, 1, 1).toEpochDay(),
        endDate: Long? = null
    ): Lease {
        return Lease(
            id = id,
            housingId = 100 + id,
            tenantId = 200 + id,
            startDateEpochDay = startDate,
            endDateEpochDay = endDate,
            rentCents = 100_000,
            chargesCents = 10_000,
            depositCents = 50_000,
            rentDueDayOfMonth = 5,
            indexAnniversaryEpochDay = anniversary
        )
    }
}
