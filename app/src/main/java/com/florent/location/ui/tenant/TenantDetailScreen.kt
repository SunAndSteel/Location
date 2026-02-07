@file:OptIn(ExperimentalMaterial3Api::class)

package com.florent.location.ui.tenant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.AdaptiveContent
import com.florent.location.ui.components.AppSectionHeader
import com.florent.location.ui.components.CardVariant
import com.florent.location.ui.components.DestructiveActionCard
import com.florent.location.ui.components.ExpressiveEmptyState
import com.florent.location.ui.components.ExpressiveErrorState
import com.florent.location.ui.components.ExpressiveLoadingState
import com.florent.location.ui.components.HeroCard
import com.florent.location.ui.components.MetricChip
import com.florent.location.ui.components.PrimaryActionRow
import com.florent.location.ui.components.windowWidthSize
import com.florent.location.ui.components.WindowWidthSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Phone
import com.florent.location.domain.model.Tenant

@Composable
fun TenantDetailScreen(
    viewModel: TenantDetailViewModel,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    TenantDetailContent(
        state = state,
        onEvent = viewModel::onEvent,
        onEdit = onEdit,
        onCreateLease = onCreateLease,
        modifier = modifier
    )
}

@Composable
private fun TenantDetailContent(
    state: TenantDetailUiState,
    onEvent: (TenantDetailUiEvent) -> Unit,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(text = "Détail du locataire") }) },
        modifier = modifier
    ) { innerPadding ->
        AdaptiveContent(innerPadding = innerPadding, contentMaxWidth = 1080.dp) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ExpressiveLoadingState(
                            title = "Chargement du locataire",
                            message = "Nous préparons la fiche du contact."
                        )
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExpressiveErrorState(
                            title = "Impossible de charger le locataire",
                            message = state.errorMessage,
                            icon = Icons.Outlined.ErrorOutline
                        )
                    }
                }

                state.isEmpty -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ExpressiveEmptyState(
                            title = "Locataire introuvable",
                            message = "Ce contact n'est plus disponible.",
                            icon = Icons.Outlined.Person
                        )
                    }
                }

                state.tenant != null -> {
                    val tenant = state.tenant
                    BoxWithConstraints {
                        val sizeClass = windowWidthSize(maxWidth)
                        if (sizeClass == WindowWidthSize.Expanded) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(0.6f),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    TenantHeroSection(tenant = tenant)
                                    TenantContactSection(tenant = tenant)
                                }
                                Column(
                                    modifier = Modifier.weight(0.4f),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    PrimaryActionRow(
                                        primaryLabel = "Modifier",
                                        onPrimary = {
                                            onEvent(TenantDetailUiEvent.Edit)
                                            onEdit()
                                        },
                                        secondaryLabel = "Créer un bail",
                                        onSecondary = onCreateLease
                                    )
                                    DestructiveActionCard(
                                        title = "Supprimer le locataire",
                                        message = "Cette action est définitive.",
                                        actionLabel = "Supprimer",
                                        onAction = { onEvent(TenantDetailUiEvent.Delete) },
                                        icon = Icons.Outlined.ReportProblem
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                TenantHeroSection(tenant = tenant)
                                TenantContactSection(tenant = tenant)
                                PrimaryActionRow(
                                    primaryLabel = "Modifier",
                                    onPrimary = {
                                        onEvent(TenantDetailUiEvent.Edit)
                                        onEdit()
                                    },
                                    secondaryLabel = "Créer un bail",
                                    onSecondary = onCreateLease
                                )
                                DestructiveActionCard(
                                    title = "Supprimer le locataire",
                                    message = "Cette action est définitive.",
                                    actionLabel = "Supprimer",
                                    onAction = { onEvent(TenantDetailUiEvent.Delete) },
                                    icon = Icons.Outlined.ReportProblem
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TenantHeroSection(
    tenant: Tenant,
    modifier: Modifier = Modifier
) {
    val status = if (tenant.phone != null || tenant.email != null) "Contact" else "Sans contact"
    val heroValue = tenant.phone ?: tenant.email ?: "Aucun"
    val heroLabel = when {
        tenant.phone != null -> "Téléphone"
        tenant.email != null -> "Email"
        else -> "Contact"
    }
    HeroCard(
        title = "${tenant.firstName} ${tenant.lastName}",
        statusBadge = status,
        heroValue = heroValue,
        heroLabel = heroLabel,
        variant = CardVariant.Highlighted,
        modifier = modifier
    )
}

@Composable
private fun TenantContactSection(
    tenant: Tenant,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppSectionHeader(
            title = "Contact",
            supportingText = "Coordonnées principales."
        )
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Coordonnées",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                tenant.phone?.let { MetricChip(label = it, icon = Icons.Outlined.Phone) }
                tenant.email?.let { MetricChip(label = it, icon = Icons.Outlined.Email) }
                if (tenant.phone == null && tenant.email == null) {
                    Text(
                        text = "Aucune information de contact enregistrée.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
