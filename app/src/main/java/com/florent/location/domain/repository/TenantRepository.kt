package com.florent.location.domain.repository

import com.florent.location.domain.model.Tenant
import kotlinx.coroutines.flow.Flow

interface TenantRepository {
    fun observeAll(): Flow<List<Tenant>>
    fun observeById(id: Long): Flow<Tenant?>
    suspend fun insert(tenant: Tenant): Long
    suspend fun update(tenant: Tenant)
    suspend fun deleteById(id: Long)
}