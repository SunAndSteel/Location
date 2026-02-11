package com.florent.location.domain.usecase.lease

import com.florent.location.domain.model.Lease
import com.florent.location.domain.repository.HousingRepository
import com.florent.location.domain.repository.LeaseRepository
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
}
