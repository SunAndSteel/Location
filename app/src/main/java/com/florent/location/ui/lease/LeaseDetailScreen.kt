package com.florent.location.ui.lease

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LeaseDetailScreen(
    state: LeaseDetailUiState,
    onEvent: (LeaseDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Détail du bail",
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
                    Text(text = "Chargement du bail...")
                }
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.lease == null -> {
                Text(text = "Bail introuvable.")
            }

            else -> {
                val lease = state.lease
                Text(text = "Logement: ${lease.housingId}")
                Text(text = "Locataire: ${lease.tenantId}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Loyer: ${lease.rentCents} cents")
                Text(text = "Charges: ${lease.chargesCents} cents")
                Text(text = "Caution: ${lease.depositCents} cents")
                Text(text = "Échéance: ${lease.rentDueDayOfMonth}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Début: ${lease.startDateEpochDay}")
                Text(text = "Fin: ${lease.endDateEpochDay ?: "Actif"}")
                lease.mailboxLabel?.let { Text(text = "Boîte aux lettres: $it") }
                lease.meterGas?.let { Text(text = "Compteur gaz: $it") }
                lease.meterElectricity?.let { Text(text = "Compteur électricité: $it") }
                lease.meterWater?.let { Text(text = "Compteur eau: $it") }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Clés", style = MaterialTheme.typography.titleMedium)

                if (state.keys.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Aucune clé enregistrée.")
                } else {
                    state.keys.forEach { key ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Type: ${key.type}")
                        key.deviceLabel?.let { Text(text = "Dispositif: $it") }
                        Text(text = "Remise: ${key.handedOverEpochDay}")
                        TextButton(onClick = { onEvent(LeaseDetailUiEvent.DeleteKeyClicked(key.id)) }) {
                            Text(text = "Supprimer la clé")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { onEvent(LeaseDetailUiEvent.AddKeyClicked) }) {
                    Text(text = "Ajouter une clé")
                }

                if (state.isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { onEvent(LeaseDetailUiEvent.CloseLeaseClicked) }) {
                        Text(text = "Clôturer le bail")
                    }
                }
            }
        }
    }

    if (state.addKeyDialog.isOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(LeaseDetailUiEvent.DismissAddKeyDialog) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(
                            LeaseDetailUiEvent.ConfirmAddKey(
                                type = state.addKeyDialog.type,
                                deviceLabel = state.addKeyDialog.deviceLabel,
                                handedOverEpochDay = state.addKeyDialog.handedOverEpochDay
                            )
                        )
                    }
                ) {
                    Text(text = "Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LeaseDetailUiEvent.DismissAddKeyDialog) }) {
                    Text(text = "Annuler")
                }
            },
            title = { Text(text = "Ajouter une clé") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.addKeyDialog.type,
                        onValueChange = {
                            onEvent(LeaseDetailUiEvent.AddKeyFieldChanged(AddKeyField.Type, it))
                        },
                        label = { Text(text = "Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.addKeyDialog.deviceLabel,
                        onValueChange = {
                            onEvent(LeaseDetailUiEvent.AddKeyFieldChanged(AddKeyField.DeviceLabel, it))
                        },
                        label = { Text(text = "Étiquette dispositif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.addKeyDialog.handedOverEpochDay,
                        onValueChange = {
                            onEvent(LeaseDetailUiEvent.AddKeyFieldChanged(AddKeyField.HandedOverEpochDay, it))
                        },
                        label = { Text(text = "Remise (epochDay)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    if (state.closeLeaseDialog.isOpen) {
        AlertDialog(
            onDismissRequest = { onEvent(LeaseDetailUiEvent.DismissCloseLeaseDialog) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(LeaseDetailUiEvent.ConfirmCloseLease(state.closeLeaseDialog.endEpochDay))
                    }
                ) {
                    Text(text = "Clôturer")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LeaseDetailUiEvent.DismissCloseLeaseDialog) }) {
                    Text(text = "Annuler")
                }
            },
            title = { Text(text = "Clôturer le bail") },
            text = {
                OutlinedTextField(
                    value = state.closeLeaseDialog.endEpochDay,
                    onValueChange = { onEvent(LeaseDetailUiEvent.CloseLeaseFieldChanged(it)) },
                    label = { Text(text = "Date de fin (epochDay)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}
