package com.florent.location.ui.housing

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HousingEditScreen(
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = if (state.housingId == null) "Nouveau logement" else "Modifier le logement",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Chargement du logement...")
            }
            return
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
        Button(onClick = { onEvent(HousingEditUiEvent.Save) }) {
            Text(text = "Enregistrer")
        }
    }
}
