package com.florent.location.data.auth

import com.florent.location.data.db.dao.AuthSessionDao
import com.florent.location.data.db.entity.AuthSessionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.auth.providers.builtin.Email

class AuthRepository(
    private val supabase: SupabaseClient,
    private val sessionDao: AuthSessionDao
) {
    suspend fun restoreSession(): Boolean {
        val local = sessionDao.getOrNull() ?: return false

        // Injecte la session dans le SDK
        supabase.auth.importSession(
            UserSession(
                accessToken = local.accessToken,
                refreshToken = local.refreshToken,
                expiresIn = 0,
                tokenType = "bearer",
                user = null
            )
        )

        // Valide si l'user existe encore
        val user = supabase.auth.currentUserOrNull()
        return if (user != null) {
            true
        } else {
            sessionDao.clear()
            false
        }
    }

    suspend fun signIn(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }

        val session = supabase.auth.currentSessionOrNull()
            ?: error("Session Supabase introuvable après login")

        val user = supabase.auth.currentUserOrNull()
            ?: error("User Supabase introuvable après login")

        val expiresAt = session.expiresAt.epochSeconds

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
        runCatching { supabase.auth.signOut() }
        sessionDao.clear()
    }
}
