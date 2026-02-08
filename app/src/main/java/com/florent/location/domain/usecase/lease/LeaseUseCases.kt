package com.florent.location.domain.usecase.lease

import com.florent.location.domain.model.Lease
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
    val rentCents: Long,
    val chargesCents: Long,
    val depositCents: Long,
    val rentDueDayOfMonth: Int
)

/**
 * Implémentation des cas d'usage basés sur le [LeaseRepository].
 */
class LeaseUseCasesImpl(
    private val repository: LeaseRepository
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

        val lease = Lease(
            id = 0L,
            housingId = housingId,
            tenantId = tenantId,
            startDateEpochDay = startDateEpochDay,
            endDateEpochDay = null,
            rentCents = request.rentCents,
            chargesCents = request.chargesCents,
            depositCents = request.depositCents,
            rentDueDayOfMonth = request.rentDueDayOfMonth,
            indexAnniversaryEpochDay = startDateEpochDay
        )

        return repository.createLease(lease)
    }

    override fun observeLease(leaseId: Long): Flow<Lease?> =
        repository.observeLease(leaseId)

    override suspend fun closeLease(leaseId: Long, endEpochDay: Long) {
        require(leaseId > 0) { "Le bail est obligatoire." }
        require(endEpochDay >= 0) { "La date de clôture est obligatoire." }
        repository.closeLease(leaseId, endEpochDay)
    }
}
