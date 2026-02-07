package com.florent.location.ui.tenant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TenantDetailScreen(
    state: TenantDetailUiState,
    onEvent: (TenantDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Détail du locataire",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Chargement du locataire...")
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.isEmpty -> {
                Text(text = "Locataire introuvable.")
            }

            state.tenant != null -> {
                Text(
                    text = "${state.tenant.firstName} ${state.tenant.lastName}",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.tenant.phone?.let { Text(text = "Téléphone: $it") }
                state.tenant.email?.let { Text(text = "Email: $it") }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { onEvent(TenantDetailUiEvent.Edit) }) {
                    Text(text = "Modifier")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { onEvent(TenantDetailUiEvent.Delete) }) {
                    Text(text = "Supprimer")
                }
            }
        }
    }
}
