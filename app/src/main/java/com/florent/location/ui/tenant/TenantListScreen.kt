@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun TenantListScreen(
    viewModel: TenantListViewModel = koinViewModel(),
    onTenantClick: (Long) -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    TenantListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onTenantClick = onTenantClick,
        onAddTenant = onAddTenant,
        modifier = modifier
    )
}

@Composable
private fun TenantListContent(
    state: TenantListUiState,
    onEvent: (TenantListUiEvent) -> Unit,
    onTenantClick: (Long) -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Locataires") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTenant,
                modifier = Modifier.focusable()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un locataire")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(TenantListUiEvent.SearchQueryChanged(it)) },
                label = { Text(text = "Rechercher") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Chargement des locataires...")
                        }
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Veuillez réessayer plus tard.")
                    }
                }

                state.isEmpty -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Aucun locataire enregistré.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onAddTenant) {
                            Text(text = "Ajouter un locataire")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.tenants, key = { it.id }) { tenant ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(TenantListUiEvent.TenantClicked(tenant.id))
                                        onTenantClick(tenant.id)
                                    }
                                    .focusable()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${tenant.firstName} ${tenant.lastName}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    tenant.phone?.let { Text(text = "Téléphone: $it") }
                                    tenant.email?.let { Text(text = "Email: $it") }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(onClick = { onTenantClick(tenant.id) }) {
                                            Text(text = "Voir")
                                        }
                                        IconButton(
                                            onClick = {
                                                onEvent(TenantListUiEvent.DeleteTenantClicked(tenant.id))
                                            },
                                            modifier = Modifier.focusable()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer le locataire"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
