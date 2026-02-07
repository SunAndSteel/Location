package com.florent.location.ui.lease

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaseCreateScreen(
    state: LeaseCreateUiState,
    onEvent: (LeaseCreateUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Création d'un bail",
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
                Text(text = "Chargement des données...")
            }
            return
        }

        ExposedDropdownMenuBox(
            expanded = state.housingDropdownExpanded,
            onExpandedChange = { onEvent(LeaseCreateUiEvent.HousingDropdownExpanded(it)) }
        ) {
            OutlinedTextField(
                value = state.housings.firstOrNull { it.id == state.selectedHousingId }
                    ?.let { "${it.address}, ${it.city}" }
                    ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(text = "Logement") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.housingDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = state.housingDropdownExpanded,
                onDismissRequest = { onEvent(LeaseCreateUiEvent.HousingDropdownExpanded(false)) }
            ) {
                state.housings.forEach { housing ->
                    DropdownMenuItem(
                        text = { Text(text = "${housing.address}, ${housing.city}") },
                        onClick = { onEvent(LeaseCreateUiEvent.SelectHousing(housing.id)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = state.tenantDropdownExpanded,
            onExpandedChange = { onEvent(LeaseCreateUiEvent.TenantDropdownExpanded(it)) }
        ) {
            OutlinedTextField(
                value = state.tenants.firstOrNull { it.id == state.selectedTenantId }
                    ?.let { "${it.firstName} ${it.lastName}" }
                    ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(text = "Locataire") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.tenantDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = state.tenantDropdownExpanded,
                onDismissRequest = { onEvent(LeaseCreateUiEvent.TenantDropdownExpanded(false)) }
            ) {
                state.tenants.forEach { tenant ->
                    DropdownMenuItem(
                        text = { Text(text = "${tenant.firstName} ${tenant.lastName}") },
                        onClick = { onEvent(LeaseCreateUiEvent.SelectTenant(tenant.id)) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.startDateEpochDay,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.StartDate, it)) },
            label = { Text(text = "Date de début (epochDay)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.rentCents,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Rent, it)) },
            label = { Text(text = "Loyer (cents)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.chargesCents,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Charges, it)) },
            label = { Text(text = "Charges (cents)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.depositCents,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Deposit, it)) },
            label = { Text(text = "Caution (cents)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.rentDueDayOfMonth,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.RentDueDay, it)) },
            label = { Text(text = "Jour d'échéance (1-28)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.mailboxLabel,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MailboxLabel, it)) },
            label = { Text(text = "Étiquette boîte aux lettres") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.meterGas,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MeterGas, it)) },
            label = { Text(text = "Compteur gaz") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.meterElectricity,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MeterElectricity, it)) },
            label = { Text(text = "Compteur électricité") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.meterWater,
            onValueChange = { onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MeterWater, it)) },
            label = { Text(text = "Compteur eau") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Clés", style = MaterialTheme.typography.titleMedium)

        state.keys.forEachIndexed { index, key ->
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = key.type,
                onValueChange = {
                    onEvent(LeaseCreateUiEvent.KeyFieldChanged(index, KeyField.Type, it))
                },
                label = { Text(text = "Type") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = key.deviceLabel,
                onValueChange = {
                    onEvent(LeaseCreateUiEvent.KeyFieldChanged(index, KeyField.DeviceLabel, it))
                },
                label = { Text(text = "Étiquette dispositif") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = key.handedOverEpochDay,
                onValueChange = {
                    onEvent(LeaseCreateUiEvent.KeyFieldChanged(index, KeyField.HandedOverEpochDay, it))
                },
                label = { Text(text = "Remise (epochDay)") },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { onEvent(LeaseCreateUiEvent.RemoveKey(index)) }) {
                Text(text = "Supprimer la clé")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { onEvent(LeaseCreateUiEvent.AddKey) }) {
            Text(text = "Ajouter une clé")
        }

        if (state.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = state.errorMessage,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (state.isSaved) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Bail enregistré.", color = MaterialTheme.colorScheme.primary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onEvent(LeaseCreateUiEvent.SaveClicked) },
            enabled = !state.isSaving
        ) {
            Text(text = if (state.isSaving) "Enregistrement..." else "Enregistrer")
        }
    }
}
