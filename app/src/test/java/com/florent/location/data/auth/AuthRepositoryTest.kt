package com.florent.location.data.auth

import com.florent.location.data.db.dao.AuthSessionDao
import com.florent.location.data.db.entity.AuthSessionEntity
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun `restoreSession calcule expiresIn depuis expiresAtEpochSeconds`() = runTest {
        val dao = FakeAuthSessionDao(
            AuthSessionEntity(
                accessToken = "access-token",
                refreshToken = "refresh-token",
                userId = "user-id",
                email = "user@mail.com",
                expiresAtEpochSeconds = 1_120L
            )
        )
        val authService = FakeAuthService().apply {
            retrievedUser = AuthUser(id = "user-id", email = "user@mail.com")
        }
        val repository = AuthRepository(
            authService = authService,
            sessionDao = dao,
            nowEpochSeconds = { 1_000L }
        )

        val restored = repository.restoreSession()

        assertTrue(restored)
        assertNotNull(authService.importedSession)
        assertEquals(120L, authService.importedSession?.expiresIn)
        assertEquals(0, authService.refreshCalls)
        assertFalse(dao.wasCleared)
    }

    @Test
    fun `restoreSession session locale expiree et refresh disponible restaure auth`() = runTest {
        val dao = FakeAuthSessionDao(
            AuthSessionEntity(
                accessToken = "expired-access-token",
                refreshToken = "refresh-token",
                userId = "old-user",
                email = "old@mail.com",
                expiresAtEpochSeconds = 900L
            )
        )
        val authService = FakeAuthService().apply {
            failFirstRetrieveUser = true
            refreshedSession = AuthSessionSnapshot(
                accessToken = "new-access-token",
                refreshToken = "new-refresh-token",
                expiresAtEpochSeconds = 3_600L
            )
            userAfterRefresh = AuthUser(id = "new-user", email = "new@mail.com")
        }
        val repository = AuthRepository(
            authService = authService,
            sessionDao = dao,
            nowEpochSeconds = { 1_000L }
        )

        val restored = repository.restoreSession()

        assertTrue(restored)
        assertEquals(1, authService.refreshCalls)
        assertFalse(dao.wasCleared)
        assertEquals("new-access-token", dao.entity?.accessToken)
        assertEquals("new-refresh-token", dao.entity?.refreshToken)
        assertEquals(3_600L, dao.entity?.expiresAtEpochSeconds)
        assertEquals("new-user", dao.entity?.userId)
    }
}

private class FakeAuthSessionDao(
    var entity: AuthSessionEntity?
) : AuthSessionDao {
    var wasCleared: Boolean = false

    override suspend fun getOrNull(): AuthSessionEntity? = entity

    override suspend fun upsert(entity: AuthSessionEntity) {
        this.entity = entity
    }

    override suspend fun clear() {
        wasCleared = true
        entity = null
    }
}

private class FakeAuthService : AuthService {
    var importedSession: UserSession? = null
    var refreshCalls: Int = 0
    var failFirstRetrieveUser: Boolean = false
    var retrievedUser: AuthUser? = null
    var userAfterRefresh: AuthUser? = null
    var refreshedSession: AuthSessionSnapshot? = null

    private var retrieveCalls = 0

    override suspend fun importSession(userSession: UserSession) {
        importedSession = userSession
    }

    override suspend fun retrieveUserForCurrentSession(): AuthUser {
        retrieveCalls++
        if (failFirstRetrieveUser && retrieveCalls == 1) {
            error("session invalide")
        }
        return userAfterRefresh ?: retrievedUser ?: error("No user available")
    }

    override suspend fun refreshCurrentSession() {
        refreshCalls++
    }

    override suspend fun signIn(email: String, password: String) = Unit

    override suspend fun signOut() = Unit

    override fun currentSessionOrNull(): AuthSessionSnapshot? = refreshedSession

    override fun currentUserOrNull(): AuthUser? = retrievedUser
}
