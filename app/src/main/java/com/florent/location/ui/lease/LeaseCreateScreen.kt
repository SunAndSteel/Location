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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.PrimaryActionRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.PersonAdd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaseCreateScreen(
    viewModel: LeaseCreateViewModel,
    onLeaseCreated: (Long) -> Unit,
    onAddHousing: () -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    LeaseCreateContent(
        state = state,
        onEvent = viewModel::onEvent,
        onLeaseCreated = onLeaseCreated,
        onAddHousing = onAddHousing,
        onAddTenant = onAddTenant,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaseCreateContent(
    state: LeaseCreateUiState,
    onEvent: (LeaseCreateUiEvent) -> Unit,
    onLeaseCreated: (Long) -> Unit,
    onAddHousing: () -> Unit,
    onAddTenant: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.isSaved, state.savedLeaseId) {
        val leaseId = state.savedLeaseId
        if (state.isSaved && leaseId != null) {
            onLeaseCreated(leaseId)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Création d'un bail") }) },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding, contentMaxWidth = 1080.dp) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ExpressiveLoadingState(
                        title = "Chargement des données",
                        message = "Nous préparons les logements et locataires."
                    )
                }
                return@AdaptiveContent
            }

            if (state.errorMessage != null) {
                ExpressiveErrorState(
                    title = "Impossible de préparer le bail",
                    message = state.errorMessage
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            val isHousingMissing = state.housings.isEmpty()
            val isTenantMissing = state.tenants.isEmpty()
            if (isHousingMissing || isTenantMissing) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Veuillez ajouter les informations requises pour créer un bail.",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (isHousingMissing) {
                            Button(onClick = onAddHousing, modifier = Modifier.focusable()) {
                                Icon(imageVector = Icons.Outlined.HomeWork, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Ajouter un logement")
                            }
                        }
                        if (isTenantMissing) {
                            Button(onClick = onAddTenant, modifier = Modifier.focusable()) {
                                Icon(imageVector = Icons.Outlined.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Ajouter un locataire")
                            }
                        }
                    }
                }
                return@AdaptiveContent
            }

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppSectionHeader(
                    title = "Sélection",
                    supportingText = "Choisissez le logement et le locataire."
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = state.housingDropdownExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = state.housingDropdownExpanded,
                        onDismissRequest = {
                            onEvent(LeaseCreateUiEvent.HousingDropdownExpanded(false))
                        }
                    ) {
                        state.housings.forEach { housing ->
                            DropdownMenuItem(
                                text = { Text(text = "${housing.address}, ${housing.city}") },
                                onClick = { onEvent(LeaseCreateUiEvent.SelectHousing(housing.id)) }
                            )
                        }
                    }
                }

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
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = state.tenantDropdownExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = state.tenantDropdownExpanded,
                        onDismissRequest = {
                            onEvent(LeaseCreateUiEvent.TenantDropdownExpanded(false))
                        }
                    ) {
                        state.tenants.forEach { tenant ->
                            DropdownMenuItem(
                                text = { Text(text = "${tenant.firstName} ${tenant.lastName}") },
                                onClick = { onEvent(LeaseCreateUiEvent.SelectTenant(tenant.id)) }
                            )
                        }
                    }
                }
                    }
                }

                AppSectionHeader(
                    title = "Conditions",
                    supportingText = "Dates et montants du bail."
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.startDate,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.StartDate, it))
                            },
                            label = { Text(text = "Date de début (yyyy-MM-dd)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.rentCents,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Rent, it))
                            },
                            label = { Text(text = "Loyer (cents)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.chargesCents,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Charges, it))
                            },
                            label = { Text(text = "Charges (cents)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.depositCents,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Deposit, it))
                            },
                            label = { Text(text = "Caution (cents)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.rentDueDayOfMonth,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.RentDueDay, it))
                            },
                            label = { Text(text = "Jour d'échéance (1-28)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AppSectionHeader(
                    title = "Compteurs",
                    supportingText = "Informations complémentaires."
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.mailboxLabel,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MailboxLabel, it))
                            },
                            label = { Text(text = "Étiquette boîte aux lettres") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.meterGas,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MeterGas, it))
                            },
                            label = { Text(text = "Compteur gaz") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.meterElectricity,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MeterElectricity, it))
                            },
                            label = { Text(text = "Compteur électricité") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = state.meterWater,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.MeterWater, it))
                            },
                            label = { Text(text = "Compteur eau") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                AppSectionHeader(
                    title = "Clés",
                    supportingText = "Clés à remettre au locataire."
                )
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        state.keys.forEachIndexed { index, key ->
                            OutlinedTextField(
                                value = key.type,
                                onValueChange = {
                                    onEvent(LeaseCreateUiEvent.KeyFieldChanged(index, KeyField.Type, it))
                                },
                                label = { Text(text = "Type") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = key.deviceLabel,
                                onValueChange = {
                                    onEvent(
                                        LeaseCreateUiEvent.KeyFieldChanged(index, KeyField.DeviceLabel, it)
                                    )
                                },
                                label = { Text(text = "Étiquette dispositif") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = key.handedOverDate,
                                onValueChange = {
                                    onEvent(
                                        LeaseCreateUiEvent.KeyFieldChanged(index, KeyField.HandedOverDate, it)
                                    )
                                },
                                label = { Text(text = "Remise (yyyy-MM-dd)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            TextButton(onClick = { onEvent(LeaseCreateUiEvent.RemoveKey(index)) }) {
                                Text(text = "Supprimer la clé")
                            }
                        }
                        TextButton(onClick = { onEvent(LeaseCreateUiEvent.AddKey) }) {
                            Text(text = "Ajouter une clé")
                        }
                    }
                }

                if (state.isSaved) {
                    Text(text = "Bail enregistré.", color = MaterialTheme.colorScheme.primary)
                }

                PrimaryActionRow(
                    primaryLabel = if (state.isSaving) "Enregistrement..." else "Enregistrer",
                    onPrimary = { onEvent(LeaseCreateUiEvent.SaveClicked) }
                )
            }
        }
    }
}
