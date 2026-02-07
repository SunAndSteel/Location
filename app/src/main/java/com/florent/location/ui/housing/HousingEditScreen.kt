package com.florent.location.ui.housing

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HousingEditScreen(
    viewModel: HousingEditViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    HousingEditContent(
        state = state,
        onEvent = viewModel::onEvent,
        onSaved = onSaved,
        modifier = modifier
    )
}

@Composable
private fun HousingEditContent(
    state: HousingEditUiState,
    onEvent: (HousingEditUiEvent) -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.housingId == null) {
                            "Nouveau logement"
                        } else {
                            "Modifier le logement"
                        }
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
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
                return@Column
            }

            OutlinedTextField(
                value = state.city,
                onValueChange = { onEvent(HousingEditUiEvent.CityChanged(it)) },
                label = { Text(text = "Ville") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.address,
                onValueChange = { onEvent(HousingEditUiEvent.AddressChanged(it)) },
                label = { Text(text = "Adresse") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.defaultRentCents.toString(),
                onValueChange = {
                    onEvent(HousingEditUiEvent.DefaultRentChanged(it.toLongOrNull() ?: 0L))
                },
                label = { Text(text = "Loyer (cents)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.defaultChargesCents.toString(),
                onValueChange = {
                    onEvent(HousingEditUiEvent.DefaultChargesChanged(it.toLongOrNull() ?: 0L))
                },
                label = { Text(text = "Charges (cents)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.depositCents.toString(),
                onValueChange = {
                    onEvent(HousingEditUiEvent.DepositChanged(it.toLongOrNull() ?: 0L))
                },
                label = { Text(text = "Caution (cents)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.peb.orEmpty(),
                onValueChange = {
                    onEvent(HousingEditUiEvent.PebChanged(it.trim().ifBlank { null }))
                },
                label = { Text(text = "PEB") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.buildingLabel.orEmpty(),
                onValueChange = {
                    onEvent(HousingEditUiEvent.BuildingLabelChanged(it.trim().ifBlank { null }))
                },
                label = { Text(text = "Bâtiment") },
                modifier = Modifier.fillMaxWidth()
            )

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onEvent(HousingEditUiEvent.Save) },
                modifier = Modifier.fillMaxWidth().focusable()
            ) {
                Text(text = "Enregistrer")
            }
        }
    }
}
