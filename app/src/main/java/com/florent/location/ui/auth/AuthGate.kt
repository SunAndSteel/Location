package com.florent.location.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.florent.location.ui.navigation.LocationNavHost
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthGate() {
    val vm: AuthGateViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    when (state) {
        AuthGateState.Loading -> {
            // tu peux mettre ton ExpressiveLoadingState ici si tu veux
        }
        AuthGateState.Unauthenticated -> {
            LoginScreen(onLoggedIn = vm::onLoggedIn)
        }
        AuthGateState.Authenticated -> {
            LocationNavHost()
        }
    }
}
