package com.florent.location.ui.tenant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TenantListScreen(
    state: TenantListUiState,
    onEvent: (TenantListUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Locataires",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onEvent(TenantListUiEvent.SearchQueryChanged(it)) },
            label = { Text(text = "Rechercher") },
            modifier = Modifier.fillMaxWidth()
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
                    Text(text = "Chargement des locataires...")
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.isEmpty -> {
                Text(text = "Aucun locataire enregistré.")
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.tenants, key = { it.id }) { tenant ->
                        Card(
                            modifier =
                                Modifier.clickable {
                                    onEvent(TenantListUiEvent.TenantClicked(tenant.id))
                                }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${tenant.firstName} ${tenant.lastName}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                tenant.phone?.let { Text(text = "Téléphone: $it") }
                                tenant.email?.let { Text(text = "Email: $it") }
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        onEvent(TenantListUiEvent.DeleteTenantClicked(tenant.id))
                                    }
                                ) {
                                    Text(text = "Supprimer")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
