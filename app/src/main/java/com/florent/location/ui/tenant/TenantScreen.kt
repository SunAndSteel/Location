package com.florent.location.ui.tenant

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun TenantScreen(
    vm: TenantViewModel = koinViewModel()
) {
    Text("Smoke test: Koin + Room OK si pas de crash ✅")
}
