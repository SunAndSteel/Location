package com.florent.location.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.florent.location.ui.navigation.LocationNavHost
import com.florent.location.ui.sync.HousingSyncStateObserver
import com.florent.location.ui.sync.SyncState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun AuthGate() {
    val vm: AuthGateViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val syncManager: HousingSyncStateObserver = koinInject()
    val syncState by syncManager.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncState) {
        val errorMessage = (syncState as? SyncState.Error)?.message
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            syncManager.consumeError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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

            if (syncState is SyncState.Syncing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}
