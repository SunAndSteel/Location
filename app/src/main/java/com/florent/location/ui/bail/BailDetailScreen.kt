@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.bail

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
fun BailDetailScreen(
    viewModel: BailDetailViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    BailDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
private fun BailDetailContent(
    state: BailDetailUiState,
    onEvent: (BailDetailUiEvent) -> Unit,
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

                state.bail == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Bail introuvable.")
                    }
                }

                else -> {
                    val bail = state.bail
                    Text(text = "Logement: ${bail.housingId}")
                    Text(text = "Locataire: ${bail.tenantId}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Loyer courant: ${bail.rentCents} cents")
                    Text(text = "Charges: ${bail.chargesCents} cents")
                    Text(text = "Caution: ${bail.depositCents} cents")
                    Text(text = "Échéance: ${bail.rentDueDayOfMonth}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Début: ${formatEpochDay(bail.startDateEpochDay)}")
                    Text(text = "Fin: ${bail.endDateEpochDay?.let { formatEpochDay(it) } ?: "Actif"}")
                    bail.mailboxLabel?.let { Text(text = "Boîte aux lettres: $it") }
                    bail.meterGas?.let { Text(text = "Compteur gaz: $it") }
                    bail.meterElectricity?.let { Text(text = "Compteur électricité: $it") }
                    bail.meterWater?.let { Text(text = "Compteur eau: $it") }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Indexation", style = MaterialTheme.typography.titleMedium)
                    state.indexationPolicy?.let { policy ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = policy.ruleLabel)
                        Text(text = "Anniversaire: ${formatEpochDay(policy.anniversaryEpochDay)}")
                        Text(text = "Prochaine échéance: ${formatEpochDay(policy.nextIndexationEpochDay)}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Simulation", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.indexationForm.indexPercent,
                        onValueChange = { onEvent(BailDetailUiEvent.IndexationPercentChanged(it)) },
                        label = { Text(text = "Indice (%)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.indexationForm.effectiveDate,
                        onValueChange = { onEvent(BailDetailUiEvent.IndexationEffectiveDateChanged(it)) },
                        label = { Text(text = "Date d'effet (yyyy-MM-dd)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onEvent(BailDetailUiEvent.SimulateIndexation) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Simuler")
                        }
                        Button(
                            onClick = { onEvent(BailDetailUiEvent.ApplyIndexation) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Appliquer")
                        }
                    }
                    state.indexationForm.simulation?.let { simulation ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Loyer actuel: ${simulation.baseRentCents} cents")
                        Text(text = "Nouveau loyer: ${simulation.newRentCents} cents")
                        Text(text = "Date d'effet: ${formatEpochDay(simulation.effectiveEpochDay)}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Historique", style = MaterialTheme.typography.titleSmall)
                    if (state.indexationHistory.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Aucune indexation appliquée.")
                    } else {
                        state.indexationHistory.forEach { event ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Le ${formatEpochDay(event.appliedEpochDay)}: " +
                                    "${event.baseRentCents} → ${event.newRentCents} cents"
                            )
                            Text(text = "Indice: ${event.indexPercent}%")
                        }
                    }

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
                                onClick = { onEvent(BailDetailUiEvent.DeleteKeyClicked(key.id)) },
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
                            onClick = { onEvent(BailDetailUiEvent.AddKeyClicked) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Ajouter une clé")
                        }
                        if (state.isActive) {
                            Button(
                                onClick = { onEvent(BailDetailUiEvent.CloseLeaseClicked) },
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
            onDismissRequest = { onEvent(BailDetailUiEvent.DismissAddKeyDialog) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(
                            BailDetailUiEvent.ConfirmAddKey(
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
                TextButton(onClick = { onEvent(BailDetailUiEvent.DismissAddKeyDialog) }) {
                    Text(text = "Annuler")
                }
            },
            title = { Text(text = "Ajouter une clé") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.addKeyDialog.type,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.Type, it))
                        },
                        label = { Text(text = "Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.addKeyDialog.deviceLabel,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.DeviceLabel, it))
                        },
                        label = { Text(text = "Étiquette dispositif") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.addKeyDialog.handedOverDate,
                        onValueChange = {
                            onEvent(BailDetailUiEvent.AddKeyFieldChanged(AddKeyField.HandedOverDate, it))
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
            onDismissRequest = { onEvent(BailDetailUiEvent.DismissCloseLeaseDialog) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(BailDetailUiEvent.ConfirmCloseLease(state.closeLeaseDialog.endDate))
                    }
                ) {
                    Text(text = "Clôturer")
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(BailDetailUiEvent.DismissCloseLeaseDialog) }) {
                    Text(text = "Annuler")
                }
            },
            title = { Text(text = "Clôturer le bail") },
            text = {
                OutlinedTextField(
                    value = state.closeLeaseDialog.endDate,
                    onValueChange = { onEvent(BailDetailUiEvent.CloseLeaseFieldChanged(it)) },
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
