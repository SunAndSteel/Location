package com.florent.location

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.florent.location.ui.auth.AuthGate
import com.florent.location.ui.navigation.LocationNavHost
import com.florent.location.ui.theme.LocationTheme

/**
 * Activité principale qui héberge l'interface Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * Configure le contenu Compose et le thème de l'application.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationTheme {
                AuthGate()
            }
        }
    }
}
