package com.florent.location.ui.lease

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
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
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.SectionHeader
import com.florent.location.ui.components.DateFieldWithPicker
import com.florent.location.ui.components.EnhancedMoneyField
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.HorizontalStepper
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.components.formatCurrency
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaseCreateScreen(
    viewModel: LeaseCreateViewModel,
    onLeaseCreated: (Long) -> Unit,
    onAddHousing: () -> Unit,
    onAddTenant: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    LeaseCreateContent(
        state = state,
        onEvent = viewModel::onEvent,
        onLeaseCreated = onLeaseCreated,
        onAddHousing = onAddHousing,
        onAddTenant = onAddTenant,
        onCancel = onCancel,
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
    onCancel: () -> Unit,
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
        navigationIcon = {
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.Outlined.Close, contentDescription = "Fermer")
            }
        },
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLowest)
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
                val selectionComplete = state.selectedHousingId != null && state.selectedTenantId != null
                val currentStep = if (selectionComplete) 1 else 0
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
                ) {
                    HorizontalStepper(
                        currentStep = currentStep,
                        totalSteps = 2,
                        stepLabels = listOf("Sélection", "Conditions"),
                        modifier = Modifier.fillMaxWidth()
                    )

                    SectionHeader(
                        title = "Sélection",
                        supportingText = "Choisissez le logement et le locataire."
                    )
                    SectionCard(tonalColor = MaterialTheme.colorScheme.surfaceContainer) {
                        EntityDropdownSelector(
                            title = "Logement",
                            displayedValue = state.housings.firstOrNull { it.id == state.selectedHousingId }
                                ?.let { it.address.fullString() }
                                ?: "Choisir un logement",
                            icon = Icons.Outlined.Home,
                            expanded = state.housingDropdownExpanded,
                            isSelected = state.selectedHousingId != null,
                            options = state.housings,
                            onExpandChange = {
                                onEvent(LeaseCreateUiEvent.HousingDropdownExpanded(it))
                            },
                            optionLabel = { housing -> housing.address.fullString() },
                            onOptionSelected = { housing ->
                                onEvent(LeaseCreateUiEvent.SelectHousing(housing.id))
                            }
                        )
                        Divider()
                        EntityDropdownSelector(
                            title = "Locataire",
                            displayedValue = state.tenants.firstOrNull { it.id == state.selectedTenantId }
                                ?.let { "${it.firstName} ${it.lastName}" }
                                ?: "Choisir un locataire",
                            icon = Icons.Outlined.Person,
                            expanded = state.tenantDropdownExpanded,
                            isSelected = state.selectedTenantId != null,
                            options = state.tenants,
                            onExpandChange = {
                                onEvent(LeaseCreateUiEvent.TenantDropdownExpanded(it))
                            },
                            optionLabel = { tenant -> "${tenant.firstName} ${tenant.lastName}" },
                            onOptionSelected = { tenant ->
                                onEvent(LeaseCreateUiEvent.SelectTenant(tenant.id))
                            }
                        )
                    }

                    SectionHeader(
                        title = "Conditions",
                        supportingText = "Dates et montants du bail."
                    )
                    SectionCard(tonalColor = MaterialTheme.colorScheme.surfaceContainer) {
                        DateFieldWithPicker(
                            label = "Date de début",
                            value = state.startDate,
                            onValueChange = {
                                onEvent(LeaseCreateUiEvent.FieldChanged(LeaseField.StartDate, it))
                            }
                        )
                        EnhancedMoneyField(
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
                            ),
                        )
                        EnhancedMoneyField(
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
                            ),
                        )
                        EnhancedMoneyField(
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
                            ),
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
                        primaryEnabled = state.canSave && !state.isSaving,
                        onPrimary = { onEvent(LeaseCreateUiEvent.SaveClicked) },
                        secondaryLabel = "Annuler",
                        onSecondary = onCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> EntityDropdownSelector(
    title: String,
    displayedValue: String,
    icon: ImageVector,
    expanded: Boolean,
    isSelected: Boolean,
    options: List<T>,
    onExpandChange: (Boolean) -> Unit,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
) {
    Box {
        ListItem(
            headlineContent = { Text(text = title) },
            supportingContent = { Text(text = displayedValue) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = null
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.surfaceContainerHighest
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandChange(true) }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = optionLabel(option)) },
                    onClick = { onOptionSelected(option) }
                )
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
