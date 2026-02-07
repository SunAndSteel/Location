@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.lease

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LeaseDetailScreen(
    viewModel: LeaseDetailViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    LeaseDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun LeaseDetailContent(
    state: LeaseDetailUiState,
    onEvent: (LeaseDetailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Détail du bail") }) },
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
                            Text(text = "Chargement du bail...")
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

                state.lease == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Bail introuvable.")
                    }
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
                    Text(text = "Début: ${formatEpochDay(lease.startDateEpochDay)}")
                    Text(text = "Fin: ${lease.endDateEpochDay?.let { formatEpochDay(it) } ?: "Actif"}")
                    lease.mailboxLabel?.let { Text(text = "Boîte aux lettres: $it") }
                    lease.meterGas?.let { Text(text = "Compteur gaz: $it") }
                    lease.meterElectricity?.let { Text(text = "Compteur électricité: $it") }
                    lease.meterWater?.let { Text(text = "Compteur eau: $it") }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
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
                            Text(text = "Remise: ${formatEpochDay(key.handedOverEpochDay)}")
                            TextButton(
                                onClick = { onEvent(LeaseDetailUiEvent.DeleteKeyClicked(key.id)) },
                                modifier = Modifier.focusable()
                            ) {
                                Text(text = "Supprimer la clé")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEvent(LeaseDetailUiEvent.AddKeyClicked) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Ajouter une clé")
                        }
                        if (state.isActive) {
                            Button(
                                onClick = { onEvent(LeaseDetailUiEvent.CloseLeaseClicked) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Clôturer le bail")
                            }
                        }
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
                                handedOverDate = state.addKeyDialog.handedOverDate
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
                        value = state.addKeyDialog.handedOverDate,
                        onValueChange = {
                            onEvent(LeaseDetailUiEvent.AddKeyFieldChanged(AddKeyField.HandedOverDate, it))
                        },
                        label = { Text(text = "Remise (yyyy-MM-dd)") },
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
                        onEvent(LeaseDetailUiEvent.ConfirmCloseLease(state.closeLeaseDialog.endDate))
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
                    value = state.closeLeaseDialog.endDate,
                    onValueChange = { onEvent(LeaseDetailUiEvent.CloseLeaseFieldChanged(it)) },
                    label = { Text(text = "Date de fin (yyyy-MM-dd)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }
}

private fun formatEpochDay(epochDay: Long): String {
    return LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ISO_LOCAL_DATE)
}
