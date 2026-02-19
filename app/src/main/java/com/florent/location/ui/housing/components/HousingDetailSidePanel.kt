package com.florent.location.ui.housing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florent.location.ui.components.EmptyDetailPane
import com.florent.location.ui.components.LabeledValueRow
import com.florent.location.ui.components.formatCurrency
import com.florent.location.ui.components.formatEpochDay
import com.florent.location.ui.housing.HousingDetailUiState
import java.time.LocalDate

@Composable
fun HousingDetailSidePanel(
    state: HousingDetailUiState,
    onCreateLease: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TenantSidePanelCard(state) }
        item { LeaseSidePanelCard(state, onCreateLease) }
        item { RentHistorySidePanelCard() }
    }
}

@Composable
private fun TenantSidePanelCard(state: HousingDetailUiState) {
    if (state.tenant == null) {
        ElevatedCard {
            EmptyDetailPane(
                title = "Aucun locataire",
                message = "Ajoutez un bail pour associer un locataire.",
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    val tenant = state.tenant
    val initials = "${tenant.firstName.firstOrNull() ?: ' '}${tenant.lastName.firstOrNull() ?: ' '}"
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("LOCATAIRE ACTUEL", style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("${tenant.firstName} ${tenant.lastName}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Locataire depuis ${state.lease?.let { formatEpochDay(it.startDateEpochDay) } ?: "-"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            LabeledValueRow("Email", tenant.email ?: "-")
            LabeledValueRow("Téléphone", tenant.phone ?: "-")
            LabeledValueRow("Dépôt de garantie", state.lease?.let { formatCurrency(it.depositCents) } ?: "-")
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Outlined.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Outlined.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LeaseSidePanelCard(state: HousingDetailUiState, onCreateLease: () -> Unit) {
    val lease = state.lease
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("BAIL EN COURS", style = MaterialTheme.typography.labelSmall)
            if (lease == null) {
                Text("Aucun bail actif")
                Button(onClick = onCreateLease) { Text("Créer un bail") }
                return@Column
            }

            val today = LocalDate.now().toEpochDay()
            val duration = (lease.endDateEpochDay ?: today) - lease.startDateEpochDay
            val elapsed = (today - lease.startDateEpochDay).coerceAtLeast(0)
            val progress = if (duration > 0) (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatEpochDay(lease.startDateEpochDay), style = MaterialTheme.typography.bodySmall)
                Text(lease.endDateEpochDay?.let { formatEpochDay(it) } ?: "Sans fin", style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            val remaining = lease.endDateEpochDay?.minus(today)
            Text(
                remaining?.let { "$it jours restants" } ?: "Bail sans terme fixe",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (remaining != null && remaining < 60) {
                AssistChip(onClick = {}, label = { Text("⚠️ Renouvellement à prévoir") })
            }
            LabeledValueRow("Loyer CC", formatCurrency(lease.rentCents + lease.chargesCents))
            LabeledValueRow("Jour d'échéance", lease.rentDueDayOfMonth.toString())
            LabeledValueRow(
                "Date révision IRL",
                lease.indexAnniversaryEpochDay?.let { formatEpochDay(it) } ?: "Non définie"
            )
        }
    }
}

@Composable
private fun RentHistorySidePanelCard() {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("LOYERS RÉCENTS", style = MaterialTheme.typography.labelSmall)
            Text(
                "Suivi des loyers à venir",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(onClick = {}) { Text("Voir les baux") }
        }
    }
}
