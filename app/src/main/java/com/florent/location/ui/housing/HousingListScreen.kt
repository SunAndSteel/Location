package com.florent.location.ui.housing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HousingListScreen(
    state: HousingListUiState,
    onEvent: (HousingListUiEvent) -> Unit,
    onHousingClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Logements",
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
                    Text(text = "Chargement des logements...")
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.isEmpty -> {
                Text(text = "Aucun logement enregistré.")
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.housings, key = { it.id }) { housing ->
                        Card(
                            modifier = Modifier.clickable { onHousingClick(housing.id) }
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
                                Button(onClick = { onHousingClick(housing.id) }) {
                                    Text(text = "Voir")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { onEvent(HousingListUiEvent.DeleteHousing(housing.id)) }) {
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
