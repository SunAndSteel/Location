package com.florent.location

import android.app.Application
import com.florent.location.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

/**
 * Application principale qui initialise l'injection de dépendances.
 */
class App : Application() {
    /**
     * Démarre Koin et charge le module de l'application.
     */
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                appModule
            )
        }
    }
}
