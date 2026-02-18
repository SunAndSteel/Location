package com.florent.location.domain.usecase.bail

import com.florent.location.domain.model.Bail
import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.IndexationPolicy
import com.florent.location.domain.model.IndexationSimulation
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.LeaseRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow

/**
 * Cas d'usage orientés bail (agrégat racine).
 */
interface BailUseCases {
    fun observeBails(): Flow<List<Bail>>
    fun observeBail(leaseId: Long): Flow<Bail?>
    /**
     * Observe le bail actif d'un logement donné. Émet null si le logement est vacant.
     */
    fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?>

    /**
     * Observe le bail actif d'un locataire donné. Émet null si pas de bail actif.
     */
    fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?>

    fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>>
    fun buildIndexationPolicy(bail: Bail, todayEpochDay: Long): IndexationPolicy
    suspend fun simulateIndexationForBail(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationSimulation

    suspend fun applyIndexationToBail(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationEvent
}

class BailUseCasesImpl(
    private val repository: LeaseRepository
) : BailUseCases {
    override fun observeBails(): Flow<List<Bail>> =
        repository.observeActiveLeases()

    override fun observeBail(leaseId: Long): Flow<Bail?> =
        repository.observeLease(leaseId)

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> =
        repository.observeActiveLeaseForHousing(housingId)

    override fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?> =
        repository.observeActiveLeaseForTenant(tenantId)

    override fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>> =
        repository.observeIndexationEvents(leaseId)

    override fun buildIndexationPolicy(bail: Bail, todayEpochDay: Long): IndexationPolicy {
        val anniversary = bail.indexAnniversaryEpochDay ?: bail.startDateEpochDay
        val nextDate = nextIndexationDate(anniversary, LocalDate.ofEpochDay(todayEpochDay))
        return IndexationPolicy(
            anniversaryEpochDay = anniversary,
            nextIndexationEpochDay = nextDate.toEpochDay()
        )
    }

    override suspend fun simulateIndexationForBail(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationSimulation {
        require(leaseId > 0) { "Le bail est obligatoire." }
        require(indexPercent >= 0) { "Le pourcentage d'indexation est invalide." }
        require(effectiveEpochDay >= 0) { "La date d'effet est obligatoire." }
        val lease = requireNotNull(repository.getLease(leaseId)) { "Bail introuvable." }
        val newRent = computeNewRent(lease.rentCents, indexPercent)
        return IndexationSimulation(
            leaseId = lease.id,
            baseRentCents = lease.rentCents,
            indexPercent = indexPercent,
            newRentCents = newRent,
            effectiveEpochDay = effectiveEpochDay
        )
    }

    override suspend fun applyIndexationToBail(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationEvent {
        require(leaseId > 0) { "Le bail est obligatoire." }
        require(indexPercent >= 0) { "Le pourcentage d'indexation est invalide." }
        require(effectiveEpochDay >= 0) { "La date d'effet est obligatoire." }
        val lease = requireNotNull(repository.getLease(leaseId)) { "Bail introuvable." }
        val newRent = computeNewRent(lease.rentCents, indexPercent)
        val event = IndexationEvent(
            leaseId = lease.id,
            appliedEpochDay = effectiveEpochDay,
            baseRentCents = lease.rentCents,
            indexPercent = indexPercent,
            newRentCents = newRent
        )
        repository.applyIndexation(event)
        return event
    }

    private fun computeNewRent(baseRentCents: Long, indexPercent: Double): Long {
        val multiplier = 1.0 + (indexPercent / 100.0)
        return kotlin.math.round(baseRentCents * multiplier).toLong()
    }

    private fun nextIndexationDate(anniversaryEpochDay: Long, today: LocalDate): LocalDate {
        val anniversaryDate = LocalDate.ofEpochDay(anniversaryEpochDay)
        if (!anniversaryDate.isBefore(today)) {
            return anniversaryDate
        }
        val yearsBetween = ChronoUnit.YEARS.between(anniversaryDate, today)
        var candidate = anniversaryDate.plusYears(yearsBetween)
        if (candidate.isBefore(today)) {
            candidate = candidate.plusYears(1)
        }
        return candidate
    }
}
