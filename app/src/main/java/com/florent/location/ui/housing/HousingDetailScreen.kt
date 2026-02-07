package com.florent.location.ui.housing

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
fun HousingDetailScreen(
    state: HousingDetailUiState,
    onEvent: (HousingDetailUiEvent) -> Unit,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Détail du logement",
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
                    Text(text = "Chargement du logement...")
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.isEmpty -> {
                Text(text = "Logement introuvable.")
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
                    Button(onClick = onEdit) {
                        Text(text = "Modifier")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onCreateLease) {
                        Text(text = "Créer un bail")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onEvent(HousingDetailUiEvent.DeleteHousing(housing.id)) }) {
                        Text(text = "Supprimer")
                    }
                }
            }
        }
    }
}
