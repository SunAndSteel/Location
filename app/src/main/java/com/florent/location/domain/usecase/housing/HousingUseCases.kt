package com.florent.location.domain.usecase.housing

import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.Key
import com.florent.location.domain.repository.HousingRepository
import kotlinx.coroutines.flow.Flow

/**
 * Cas d'usage disponibles pour la gestion des logements.
 */
interface HousingUseCases {
    /**
     * Observe l'ensemble des logements.
     */
    fun observeHousings(): Flow<List<Housing>>

    /**
     * Observe un logement par identifiant.
     */
    fun observeHousing(id: Long): Flow<Housing?>

    /**
     * Crée un logement et renvoie l'identifiant.
     */
    suspend fun createHousing(housing: Housing): Long

    /**
     * Met à jour un logement.
     */
    suspend fun updateHousing(housing: Housing)

    /**
     * Supprime un logement par identifiant.
     * La suppression est interdite si un bail actif existe.
     */
    suspend fun deleteHousing(id: Long)

    /**
     * Observe les clés associées à un logement.
     */
    fun observeKeysForHousing(housingId: Long): Flow<List<Key>>

    /**
     * Ajoute une clé à un logement.
     */
    suspend fun addKey(housingId: Long, key: Key): Long

    /**
     * Supprime une clé.
     */
    suspend fun deleteKey(keyId: Long)
}

/**
 * Implémentation des cas d'usage basés sur le [HousingRepository].
 */
class HousingUseCasesImpl(
    private val repository: HousingRepository
) : HousingUseCases {

    /**
     * Observe tous les logements.
     */
    override fun observeHousings(): Flow<List<Housing>> =
        repository.observeHousings()

    /**
     * Observe un logement par identifiant.
     */
    override fun observeHousing(id: Long): Flow<Housing?> =
        repository.observeHousing(id)

    /**
     * Crée un logement et renvoie l'identifiant.
     */
    override suspend fun createHousing(housing: Housing): Long =
        repository.insert(housing)

    /**
     * Met à jour un logement.
     */
    override suspend fun updateHousing(housing: Housing) {
        repository.update(housing)
    }

    /**
     * Supprime un logement si aucun bail actif n'existe.
     */
    override suspend fun deleteHousing(id: Long) {
        require(!repository.hasActiveLease(id)) {
            "Suppression impossible: un bail actif existe pour ce logement."
        }
        repository.deleteById(id)
    }

    override fun observeKeysForHousing(housingId: Long): Flow<List<Key>> =
        repository.observeKeysForHousing(housingId)

    override suspend fun addKey(housingId: Long, key: Key): Long {
        require(housingId > 0) { "Le logement est obligatoire." }
        val type = key.type.trim()
        require(type.isNotBlank()) { "Le type de clé est obligatoire." }
        require(key.handedOverEpochDay >= 0) { "La date de remise est obligatoire." }

        val normalized = key.copy(
            housingId = housingId,
            type = type,
            deviceLabel = key.deviceLabel?.trim()?.ifBlank { null }
        )

        return repository.insertKey(normalized)
    }

    override suspend fun deleteKey(keyId: Long) {
        require(keyId > 0) { "Clé invalide." }
        repository.deleteKeyById(keyId)
    }
}
