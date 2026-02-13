package com.florent.location.data.auth

import com.florent.location.data.db.dao.AuthSessionDao
import com.florent.location.data.db.entity.AuthSessionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserSession
import kotlin.math.max

class AuthRepository(
    private val authService: AuthService,
    private val sessionDao: AuthSessionDao,
    private val nowEpochSeconds: () -> Long = { System.currentTimeMillis() / 1_000 }
) {
    constructor(
        supabase: SupabaseClient,
        sessionDao: AuthSessionDao,
        nowEpochSeconds: () -> Long = { System.currentTimeMillis() / 1_000 }
    ) : this(
        authService = SupabaseAuthService(supabase),
        sessionDao = sessionDao,
        nowEpochSeconds = nowEpochSeconds
    )

    suspend fun restoreSession(): Boolean {
        val local = sessionDao.getOrNull() ?: return false

        authService.importSession(
            UserSession(
                accessToken = local.accessToken,
                refreshToken = local.refreshToken,
                expiresIn = computeExpiresIn(local.expiresAtEpochSeconds),
                tokenType = "bearer",
                user = null
            )
        )

        val validatedUser = runCatching { authService.retrieveUserForCurrentSession() }.getOrNull()
        if (validatedUser != null) return true

        val refreshSucceeded = runCatching { authService.refreshCurrentSession() }.isSuccess
        if (!refreshSucceeded) {
            sessionDao.clear()
            return false
        }

        val refreshedUser = runCatching { authService.retrieveUserForCurrentSession() }.getOrNull()
        val refreshedSession = authService.currentSessionOrNull()

        if (refreshedUser == null || refreshedSession == null) {
            sessionDao.clear()
            return false
        }

        sessionDao.upsert(
            AuthSessionEntity(
                accessToken = refreshedSession.accessToken,
                refreshToken = refreshedSession.refreshToken,
                userId = refreshedUser.id,
                email = refreshedUser.email,
                expiresAtEpochSeconds = refreshedSession.expiresAtEpochSeconds
            )
        )

        return true
    }

    suspend fun signIn(email: String, password: String) {
        authService.signIn(email = email, password = password)

        val session = authService.currentSessionOrNull()
            ?: error("Session Supabase introuvable après login")

        val user = authService.currentUserOrNull()
            ?: error("User Supabase introuvable après login")

        val expiresAt = session.expiresAtEpochSeconds

        sessionDao.upsert(
            AuthSessionEntity(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                userId = user.id,
                email = user.email,
                expiresAtEpochSeconds = expiresAt
            )
        )
    }

    suspend fun signOut() {
        runCatching { authService.signOut() }
        sessionDao.clear()
    }

    private fun computeExpiresIn(expiresAtEpochSeconds: Long): Long {
        val remainingSeconds = expiresAtEpochSeconds - nowEpochSeconds()
        return max(remainingSeconds, 0L)
    }
}


data class AuthUser(
    val id: String,
    val email: String?
)

data class AuthSessionSnapshot(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long
)

interface AuthService {
    suspend fun importSession(userSession: UserSession)
    suspend fun retrieveUserForCurrentSession(): AuthUser
    suspend fun refreshCurrentSession()
    suspend fun signIn(email: String, password: String)
    suspend fun signOut()
    fun currentSessionOrNull(): AuthSessionSnapshot?
    fun currentUserOrNull(): AuthUser?
}

class SupabaseAuthService(
    private val supabase: SupabaseClient
) : AuthService {
    override suspend fun importSession(userSession: UserSession) {
        supabase.auth.importSession(session = userSession)
    }

    override suspend fun retrieveUserForCurrentSession(): AuthUser {
        val user = supabase.auth.retrieveUserForCurrentSession()
        return AuthUser(id = user.id, email = user.email)
    }

    override suspend fun refreshCurrentSession() {
        supabase.auth.refreshCurrentSession()
    }

    override suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signOut() {
        supabase.auth.signOut()
    }

    override fun currentSessionOrNull(): AuthSessionSnapshot? = supabase.auth.currentSessionOrNull()?.let {
        AuthSessionSnapshot(
            accessToken = it.accessToken,
            refreshToken = it.refreshToken,
            expiresAtEpochSeconds = it.expiresAt.epochSeconds
        )
    }

    override fun currentUserOrNull(): AuthUser? = supabase.auth.currentUserOrNull()?.let {
        AuthUser(id = it.id, email = it.email)
    }
}
