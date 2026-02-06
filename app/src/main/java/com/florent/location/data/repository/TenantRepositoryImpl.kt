package com.florent.location.data.repository

import com.florent.location.data.db.dao.TenantDao
import com.florent.location.domain.model.Tenant
import com.florent.location.domain.repository.TenantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TenantRepositoryImpl (
    private val dao: TenantDao
) : TenantRepository {
    override fun observeAll(): Flow<List<Tenant>> =
        dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeById(id: Long): Flow<Tenant?> =
        dao.observeById(id).map { entity ->
            entity?.toDomain()
        }

    override suspend fun insert(tenant: Tenant): Long =
        dao.insert(tenant.toEntity())

    override suspend fun update(tenant: Tenant) =
        dao.update(tenant.toEntity())

    override suspend fun deleteById(id: Long) =
        dao.deleteById(id)
}