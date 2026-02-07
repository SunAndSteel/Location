package com.florent.location.ui.housing

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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

@Composable
fun HousingDetailScreen(
    viewModel: HousingDetailViewModel,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    HousingDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onEdit = onEdit,
        onCreateLease = onCreateLease,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HousingDetailContent(
    state: HousingDetailUiState,
    onEvent: (HousingDetailUiEvent) -> Unit,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Détail du logement") }) },
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
                            Text(text = "Chargement du logement...")
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
                        Text(text = "Logement introuvable.")
                    }
                }

                else -> {
                    val housing = state.housing
                    if (housing != null) {
                        Text(
                            text = housing.address,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(text = housing.city)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Loyer: ${housing.defaultRentCents} cents")
                        Text(text = "Charges: ${housing.defaultChargesCents} cents")
                        Text(text = "Caution: ${housing.depositCents} cents")
                        housing.peb?.let { Text(text = "PEB: $it") }
                        housing.buildingLabel?.let { Text(text = "Bâtiment: $it") }
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                                Text(text = "Modifier")
                            }
                            Button(onClick = onCreateLease, modifier = Modifier.weight(1f)) {
                                Text(text = "Créer un bail")
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onEvent(HousingDetailUiEvent.DeleteHousing(housing.id)) },
                            modifier = Modifier.fillMaxWidth().focusable()
                        ) {
                            Text(text = "Supprimer")
                        }
                    }
                }
            }
        }
    }
}
