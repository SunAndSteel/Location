@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.ScreenScaffold
import com.florent.location.ui.components.SectionCard
import com.florent.location.ui.components.tenantStatusLabel
import com.florent.location.ui.components.UiTokens
import com.florent.location.ui.lease.ExposedDropdownMenu
import com.florent.location.domain.model.TenantStatus

@Composable
fun TenantEditScreen(
    viewModel: TenantEditViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    TenantEditContent(
        state = state,
        onEvent = viewModel::onEvent,
        onSaved = onSaved,
        modifier = modifier
    )
}

@Composable
private fun TenantEditContent(
    state: TenantEditUiState,
    onEvent: (TenantEditUiEvent) -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    ScreenScaffold(
        title = if (state.tenantId == null) "Nouveau locataire" else "Modifier le locataire",
        contentMaxWidth = UiTokens.ContentMaxWidthMedium,
        modifier = modifier
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ExpressiveLoadingState(
                    title = "Chargement du locataire",
                    message = "Nous préparons le formulaire."
                )
            }
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingL)
            ) {
                AppSectionHeader(
                    title = "Identité",
                    supportingText = "Informations principales du locataire."
                )
                SectionCard {
                    OutlinedTextField(
                        value = state.firstName,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.FirstName, it))
                        },
                        label = { Text(text = "Prénom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.lastName,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.LastName, it))
                        },
                        label = { Text(text = "Nom") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AppSectionHeader(
                    title = "Statut",
                    supportingText = "Situation du locataire."
                )
                SectionCard {
                    ExposedDropdownMenuBox(
                        expanded = state.statusDropdownExpanded,
                        onExpandedChange = { onEvent(TenantEditUiEvent.StatusDropdownExpanded(it)) }
                    ) {
                        OutlinedTextField(
                            value = tenantStatusLabel(state.status),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(text = "Statut") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = state.statusDropdownExpanded
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = state.statusDropdownExpanded,
                            onDismissRequest = {
                                onEvent(TenantEditUiEvent.StatusDropdownExpanded(false))
                            }
                        ) {
                            TenantStatus.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(text = tenantStatusLabel(status)) },
                                    onClick = {
                                        onEvent(TenantEditUiEvent.StatusChanged(status))
                                        onEvent(TenantEditUiEvent.StatusDropdownExpanded(false))
                                    }
                                )
                            }
                        }
                    }
                }

                AppSectionHeader(
                    title = "Contact",
                    supportingText = "Coordonnées du locataire."
                )
                SectionCard {
                    OutlinedTextField(
                        value = state.phone,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.Phone, it))
                        },
                        label = { Text(text = "Téléphone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = {
                            onEvent(TenantEditUiEvent.FieldChanged(TenantField.Email, it))
                        },
                        label = { Text(text = "Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                PrimaryActionRow(
                    primaryLabel = "Enregistrer",
                    onPrimary = { onEvent(TenantEditUiEvent.SaveClicked) }
                )
            }
        }
    }
}
