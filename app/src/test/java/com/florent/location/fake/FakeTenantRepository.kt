package com.florent.location.fake

import com.florent.location.domain.model.Tenant
import com.florent.location.domain.repository.TenantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTenantRepository(
    initialTenants: List<Tenant> = emptyList()
) : TenantRepository {
    private val tenantsFlow = MutableStateFlow(initialTenants)
    private var nextId: Long =
        (initialTenants.maxOfOrNull { it.id } ?: 0L) + 1L
    private val activeLeaseTenantIds = mutableSetOf<Long>()

    override fun observeTenants(): Flow<List<Tenant>> = tenantsFlow

    override fun observeTenant(id: Long): Flow<Tenant?> =
        tenantsFlow.map { tenants -> tenants.firstOrNull { it.id == id } }

    override suspend fun createTenant(tenant: Tenant): Long {
        val id = if (tenant.id == 0L) nextId++ else tenant.id
        val newTenant = tenant.copy(id = id)
        tenantsFlow.value = tenantsFlow.value + newTenant
        return id
    }

    override suspend fun updateTenant(tenant: Tenant) {
        val tenants = tenantsFlow.value.toMutableList()
        val index = tenants.indexOfFirst { it.id == tenant.id }
        require(index != -1) { "Locataire introuvable." }
        tenants[index] = tenant
        tenantsFlow.value = tenants
    }

    override suspend fun deleteTenant(id: Long) {
        require(id !in activeLeaseTenantIds) {
            "Impossible de supprimer un locataire avec un bail actif."
        }
        tenantsFlow.value = tenantsFlow.value.filterNot { it.id == id }
    }

    fun setTenants(tenants: List<Tenant>) {
        tenantsFlow.value = tenants
    }

    fun setActiveLease(tenantId: Long, hasActiveLease: Boolean) {
        if (hasActiveLease) {
            activeLeaseTenantIds.add(tenantId)
        } else {
            activeLeaseTenantIds.remove(tenantId)
        }
    }
}
