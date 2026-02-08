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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.DateFieldWithPicker
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.MoneyField
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.formatCurrency
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

    ScreenScaffold(
        title = "Créer un bail",
        contentMaxWidth = UiTokens.ContentMaxWidthExpanded,
        modifier = modifier
    ) {
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
        } else {
            if (state.errorMessage != null) {
                ExpressiveErrorState(
                    title = "Impossible de préparer le bail",
                    message = state.errorMessage
                )
                Spacer(modifier = Modifier.height(UiTokens.SpacingL))
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
                    Spacer(modifier = Modifier.height(UiTokens.SpacingS))
                    Row(horizontalArrangement = Arrangement.spacedBy(UiTokens.SpacingS)) {
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
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                ) {
            AppSectionHeader(
                title = "Sélection",
                supportingText = "Choisissez le logement et le locataire."
            )
            SectionCard {
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

            AppSectionHeader(
                title = "Conditions",
                supportingText = "Dates et montants du bail."
            )
            SectionCard {
                DateFieldWithPicker(
                    label = "Date de début",
                    value = state.startDate,
                    onValueChange = {
                        onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.StartDate, it))
                    }
                )
                MoneyField(
                    label = "Loyer (€)",
                    value = state.rent,
                    onValueChange = {
                        onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Rent, it))
                    },
                    supportingText = buildAmountSupportingText(
                        selectedHousingId = state.selectedHousingId,
                        housingDefaultCents = state.housingDefaultRentCents,
                        overridden = state.rentOverridden,
                        fallback = "Saisissez un montant en euros, ex: 750,00"
                    )
                )
                MoneyField(
                    label = "Charges (€)",
                    value = state.charges,
                    onValueChange = {
                        onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Charges, it))
                    },
                    supportingText = buildAmountSupportingText(
                        selectedHousingId = state.selectedHousingId,
                        housingDefaultCents = state.housingDefaultChargesCents,
                        overridden = state.chargesOverridden,
                        fallback = "Saisissez un montant en euros, ex: 120,00"
                    )
                )
                MoneyField(
                    label = "Caution (€)",
                    value = state.deposit,
                    onValueChange = {
                        onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.Deposit, it))
                    },
                    supportingText = buildAmountSupportingText(
                        selectedHousingId = state.selectedHousingId,
                        housingDefaultCents = state.housingDepositCents,
                        overridden = state.depositOverridden,
                        fallback = "Saisissez un montant en euros, ex: 900,00"
                    )
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

            if (state.isSaved) {
                Text(text = "Bail enregistré.", color = MaterialTheme.colorScheme.primary)
            }

            PrimaryActionRow(
                primaryLabel = if (state.isSaving) "Enregistrement..." else "Créer le bail",
                onPrimary = { onEvent(LeaseCreateUiEvent.SaveClicked) }
            )
                }
            }
        }
    }
}

private fun buildAmountSupportingText(
    selectedHousingId: Long?,
    housingDefaultCents: Long,
    overridden: Boolean,
    fallback: String
): String {
    if (selectedHousingId == null) return fallback
    val defaultLabel = "Valeur logement : ${formatCurrency(housingDefaultCents)}"
    return if (overridden) {
        "Personnalisé • $defaultLabel"
    } else {
        defaultLabel
    }
}
