package com.florent.location.domain.usecase


import com.florent.location.domain.model.Tenant
import com.florent.location.domain.repository.TenantRepository
import kotlinx.coroutines.flow.Flow


interface TenantUseCases {
    fun observeAll(): Flow<List<Tenant>>
    fun observeById(id: Long): Flow<Tenant?>
    suspend fun create(tenant: Tenant): Long
    suspend fun update(tenant: Tenant)
    suspend fun delete(id: Long)
}

class TenantUseCasesImpl(
    private val repository: TenantRepository
) : TenantUseCases {

    override fun observeAll(): Flow<List<Tenant>> =
        repository.observeAll()

    override fun observeById(id: Long): Flow<Tenant?> =
        repository.observeById(id)

    override suspend fun create(tenant: Tenant): Long =
        repository.insert(tenant)

    override suspend fun update(tenant: Tenant) =
        repository.update(tenant)

    override suspend fun delete(id: Long) =
        repository.deleteById(id)
}
