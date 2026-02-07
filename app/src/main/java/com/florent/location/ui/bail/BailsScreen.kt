package com.florent.location.ui.bail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

@ExperimentalMaterial3Api
@Composable
fun BailsScreen(
    viewModel: BailsViewModel = koinViewModel(),
    onBailClick: (Long) -> Unit,
    onAddBail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    BailsContent(
        state = state,
        onBailClick = onBailClick,
        onAddBail = onAddBail,
        modifier = modifier
    )
}

@ExperimentalMaterial3Api
@Composable
private fun BailsContent(
    state: BailsUiState,
    onBailClick: (Long) -> Unit,
    onAddBail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Baux") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBail, modifier = Modifier.focusable()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un bail")
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
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Chargement des baux...")
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
                        Text(text = "Aucun bail enregistré.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onAddBail) {
                            Text(text = "Créer un bail")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.bails, key = { it.id }) { bail ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBailClick(bail.id) }
                                    .focusable()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Bail #${bail.id}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(text = "Logement: ${bail.housingId}")
                                    Text(text = "Locataire: ${bail.tenantId}")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Loyer: ${bail.rentCents} cents")
                                        Button(onClick = { onBailClick(bail.id) }) {
                                            Text(text = "Voir")
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
