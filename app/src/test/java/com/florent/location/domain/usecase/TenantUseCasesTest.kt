package com.florent.location.domain.usecase

import com.florent.location.domain.model.Tenant
import com.florent.location.domain.usecase.tenant.TenantUseCasesImpl
import com.florent.location.fake.FakeTenantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TenantUseCasesTest {

    @Test
    fun `createTenant rejects blank first name`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)

        try {
            useCases.createTenant(Tenant(0L, "", "Doe", null, null))
            throw AssertionError("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals("Le prénom est obligatoire.", error.message)
        }
    }

    @Test
    fun `createTenant rejects blank last name`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)

        try {
            useCases.createTenant(Tenant(0L, "Jane", " ", null, null))
            throw AssertionError("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals("Le nom est obligatoire.", error.message)
        }
    }

    @Test
    fun `createTenant inserts tenant and returns id`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)

        val id = useCases.createTenant(Tenant(0L, "Jane", "Doe", "123", "jane@doe.com"))

        assertEquals(1L, id)
        val tenants = repository.observeTenants().first()
        assertEquals(1, tenants.size)
    }

    @Test
    fun `updateTenant updates existing tenant`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)

        val id = useCases.createTenant(Tenant(0L, "Jane", "Doe", null, null))
        useCases.updateTenant(Tenant(id, "Janet", "Doe", "555", null))

        val tenant = repository.observeTenant(id).first()
        assertEquals("Janet", tenant?.firstName)
        assertEquals("555", tenant?.phone)
    }

    @Test
    fun `deleteTenant succeeds when no active lease exists`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)

        val id = useCases.createTenant(Tenant(0L, "Jane", "Doe", null, null))
        useCases.deleteTenant(id)

        val tenants = repository.observeTenants().first()
        assertTrue(tenants.isEmpty())
    }

    @Test
    fun `deleteTenant fails when active lease exists`() = runTest {
        val repository = FakeTenantRepository()
        val useCases = TenantUseCasesImpl(repository)

        val id = useCases.createTenant(Tenant(0L, "Jane", "Doe", null, null))
        repository.setActiveLease(id, true)

        try {
            useCases.deleteTenant(id)
            throw AssertionError("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals(
                "Impossible de supprimer un locataire avec un bail actif.",
                error.message
            )
        }
    }
}
