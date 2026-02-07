package com.florent.location.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.florent.location.data.db.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour manipuler les locataires.
 */
@Dao
interface TenantDao {

    /**
     * Retourne le flux de tous les locataires triés par nom.
     */
    @Query("SELECT * FROM tenants ORDER BY lastName ASC, firstName ASC")
    fun observeAll(): Flow<List<TenantEntity>>

    /**
     * Retourne le flux d'un locataire par identifiant.
     */
    @Query("SELECT * FROM tenants WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<TenantEntity?>

    /**
     * Insère une entité et renvoie l'identifiant généré.
     */
    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(entity: TenantEntity): Long

    /**
     * Met à jour une entité existante.
     */
    @Update
    suspend fun update(entity: TenantEntity)

    /**
     * Supprime un locataire par identifiant.
     */
    @Query("DELETE FROM tenants WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Vérifie si un locataire possède un bail actif.
     */
    @Query(
        """
        SELECT COUNT(*) > 0 FROM leases
        WHERE tenantId = :tenantId
          AND endDateEpochDay IS NULL
        """
    )
    suspend fun hasActiveLease(tenantId: Long): Boolean
}
