package com.florent.location.domain.usecase.lease

import com.florent.location.domain.model.IndexationEvent
import com.florent.location.domain.model.IndexationPolicy
import com.florent.location.domain.model.IndexationSimulation
import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.HousingRepository
import com.florent.location.domain.repository.LeaseRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow

/**
 * Cas d'usage disponibles pour la gestion des baux.
 */
interface LeaseUseCases {
    /**
     * Crée un bail avec ses clés associées.
     */
    suspend fun createLease(request: LeaseCreateRequest): Long

    /**
     * Observe un bail.
     */
    fun observeLease(leaseId: Long): Flow<Lease?>

    /**
     * Observe tous les baux actifs.
     */
    fun observeLeases(): Flow<List<Lease>>

    /**
     * Observe le bail actif pour un logement.
     */
    fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?>

    /**
     * Observe le bail actif pour un locataire.
     */
    fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?>

    /**
     * Observe l'historique d'indexation d'un bail.
     */
    fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>>

    /**
     * Construit la politique d'indexation d'un bail.
     */
    fun buildIndexationPolicy(lease: Lease, todayEpochDay: Long): IndexationPolicy

    /**
     * Simule une indexation pour un bail.
     */
    suspend fun simulateIndexation(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationSimulation

    /**
     * Applique une indexation sur un bail.
     */
    suspend fun applyIndexation(
        leaseId: Long,
        indexPercent: Double,
        effectiveEpochDay: Long
    ): IndexationEvent

    /**
     * Clôture un bail existant.
     */
    suspend fun closeLease(leaseId: Long, endEpochDay: Long)
}

data class LeaseCreateRequest(
    val housingId: Long?,
    val tenantId: Long?,
    val startDateEpochDay: Long?,
    val rentCents: Long?,
    val chargesCents: Long?,
    val depositCents: Long?,
    val rentDueDayOfMonth: Int
)

/**
 * Implémentation des cas d'usage basés sur le [LeaseRepository].
 */
class LeaseUseCasesImpl(
    private val repository: LeaseRepository,
    private val housingRepository: HousingRepository
) : LeaseUseCases {

    override suspend fun createLease(request: LeaseCreateRequest): Long {
        val housingId = request.housingId ?: 0L
        val tenantId = request.tenantId ?: 0L
        val startDateEpochDay = request.startDateEpochDay
        require(housingId > 0) { "Le logement est obligatoire." }
        require(tenantId > 0) { "Le locataire est obligatoire." }
        require(startDateEpochDay != null) { "La date de début est obligatoire." }
        require(request.rentDueDayOfMonth in 1..28) { "Le jour d'échéance doit être entre 1 et 28." }
        require(repository.housingExists(housingId)) { "Le logement sélectionné n'existe pas." }
        require(repository.tenantExists(tenantId)) { "Le locataire sélectionné n'existe pas." }
        val housing = requireNotNull(housingRepository.getHousing(housingId)) {
            "Le logement sélectionné n'existe pas."
        }
        val defaultRentCents = housing.rentCents
        val defaultChargesCents = housing.chargesCents
        val defaultDepositCents = housing.depositCents
        val rentCents = request.rentCents ?: defaultRentCents
        val chargesCents = request.chargesCents ?: defaultChargesCents
        val depositCents = request.depositCents ?: defaultDepositCents

        val lease = Lease(
            id = 0L,
            housingId = housingId,
            tenantId = tenantId,
            startDateEpochDay = startDateEpochDay,
            endDateEpochDay = null,
            rentCents = rentCents,
            chargesCents = chargesCents,
            depositCents = depositCents,
            rentDueDayOfMonth = request.rentDueDayOfMonth,
            indexAnniversaryEpochDay = startDateEpochDay,
            rentOverridden = request.rentCents?.let { it != defaultRentCents } ?: false,
            chargesOverridden = request.chargesCents?.let { it != defaultChargesCents } ?: false,
            depositOverridden = request.depositCents?.let { it != defaultDepositCents } ?: false,
            housingRentCentsSnapshot = defaultRentCents,
            housingChargesCentsSnapshot = defaultChargesCents,
            housingDepositCentsSnapshot = defaultDepositCents
        )

        return repository.createLease(lease)
    }

    override fun observeLease(leaseId: Long): Flow<Lease?> =
        repository.observeLease(leaseId)

    override fun observeLeases(): Flow<List<Lease>> =
        repository.observeActiveLeases()

    override fun observeActiveLeaseForHousing(housingId: Long): Flow<Lease?> =
        repository.observeActiveLeaseForHousing(housingId)

    override fun observeActiveLeaseForTenant(tenantId: Long): Flow<Lease?> =
        repository.observeActiveLeaseForTenant(tenantId)

    override fun observeIndexationEvents(leaseId: Long): Flow<List<IndexationEvent>> =
        repository.observeIndexationEvents(leaseId)

    override fun buildIndexationPolicy(lease: Lease, todayEpochDay: Long): IndexationPolicy {
        val anniversary = lease.indexAnniversaryEpochDay ?: lease.startDateEpochDay
        val nextDate = nextIndexationDate(anniversary, LocalDate.ofEpochDay(todayEpochDay))
        return IndexationPolicy(
            anniversaryEpochDay = anniversary,
            nextIndexationEpochDay = nextDate.toEpochDay()
        )
    }

    override suspend fun simulateIndexation(
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

    override suspend fun applyIndexation(
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

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        require(leaseId > 0) { "Le bail est obligatoire." }
        require(endEpochDay >= 0) { "La date de clôture est obligatoire." }
        val lease = requireNotNull(repository.getLease(leaseId)) {
            "Bail introuvable."
        }
        require(endEpochDay >= lease.startDateEpochDay) {
            "La date de clôture ne peut pas être antérieure à la date de début du bail."
        }
        repository.closeLease(leaseId, endEpochDay)
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
