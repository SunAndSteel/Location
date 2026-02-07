@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.PrimaryActionRow

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.tenantId == null) {
                            "Nouveau locataire"
                        } else {
                            "Modifier le locataire"
                        }
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding, contentMaxWidth = 960.dp) {
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
                return@AdaptiveContent
            }

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppSectionHeader(
                    title = "Identité",
                    supportingText = "Informations principales du locataire."
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
                }

                AppSectionHeader(
                    title = "Contact",
                    supportingText = "Coordonnées du locataire."
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
