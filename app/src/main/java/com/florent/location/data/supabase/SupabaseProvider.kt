package com.florent.location.data.supabase

import com.florent.location.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

fun provideSupabaseClient() = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_PUB_KEY // <= ta clé "anon/publishable"
) {
    install(Auth)
    install(Postgrest)
}
