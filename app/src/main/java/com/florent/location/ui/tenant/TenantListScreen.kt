@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.material3.ExperimentalMaterial3Api
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.TenantCard
import com.florent.location.ui.components.keyboardClickable

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
        AdaptiveContent(innerPadding = innerPadding) {
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
                            val openTenant = {
                                onEvent(TenantListUiEvent.TenantClicked(tenant.id))
                                onTenantClick(tenant.id)
                            }
                            TenantCard(
                                tenant = tenant,
                                onOpen = openTenant,
                                onDelete = {
                                    onEvent(TenantListUiEvent.DeleteTenantClicked(tenant.id))
                                },
                                isSelected = tenant.id == state.selectedTenantId,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .keyboardClickable(onClick = openTenant)
                            )
                        }
                    }
                }
            }
        }
    }
}
