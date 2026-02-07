package com.florent.location.ui.housing

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import org.koin.androidx.compose.koinViewModel

@Composable
fun HousingListScreen(
    viewModel: HousingListViewModel = koinViewModel(),
    onHousingClick: (Long) -> Unit,
    onAddHousing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    HousingListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onHousingClick = onHousingClick,
        onAddHousing = onAddHousing,
        modifier = modifier
    )
}

@Composable
private fun HousingListContent(
    state: HousingListUiState,
    onEvent: (HousingListUiEvent) -> Unit,
    onHousingClick: (Long) -> Unit,
    onAddHousing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Logements") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHousing, modifier = Modifier.focusable()) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Ajouter un logement")
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
                            Text(text = "Chargement des logements...")
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
                        Text(text = "Aucun logement enregistré.")
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onAddHousing) {
                            Text(text = "Ajouter un logement")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.housings, key = { it.id }) { housing ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onHousingClick(housing.id) }
                                    .focusable()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = housing.address,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(text = housing.city)
                                    Text(text = "Loyer: ${housing.defaultRentCents} cents")
                                    Text(text = "Charges: ${housing.defaultChargesCents} cents")
                                    Text(text = "Caution: ${housing.depositCents} cents")
                                    housing.peb?.let { Text(text = "PEB: $it") }
                                    housing.buildingLabel?.let { Text(text = "Bâtiment: $it") }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(onClick = { onHousingClick(housing.id) }) {
                                            Text(text = "Voir")
                                        }
                                        IconButton(
                                            onClick = {
                                                onEvent(HousingListUiEvent.DeleteHousing(housing.id))
                                            },
                                            modifier = Modifier.focusable()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Supprimer le logement"
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
