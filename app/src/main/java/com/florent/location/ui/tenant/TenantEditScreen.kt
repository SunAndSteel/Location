@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Chargement du locataire...")
                    }
                }
                return@Column
            }

            OutlinedTextField(
                value = state.firstName,
                onValueChange = { onEvent(TenantEditUiEvent.FieldChanged(TenantField.FirstName, it)) },
                label = { Text(text = "Prénom") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.lastName,
                onValueChange = { onEvent(TenantEditUiEvent.FieldChanged(TenantField.LastName, it)) },
                label = { Text(text = "Nom") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.phone,
                onValueChange = { onEvent(TenantEditUiEvent.FieldChanged(TenantField.Phone, it)) },
                label = { Text(text = "Téléphone") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(TenantEditUiEvent.FieldChanged(TenantField.Email, it)) },
                label = { Text(text = "Email") },
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
            Button(
                onClick = { onEvent(TenantEditUiEvent.SaveClicked) },
                modifier = Modifier.fillMaxWidth().focusable()
            ) {
                Text(text = "Enregistrer")
            }
        }
    }
}
