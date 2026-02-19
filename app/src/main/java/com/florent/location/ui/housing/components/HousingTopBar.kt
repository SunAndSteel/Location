package com.florent.location.ui.housing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HousingTopBar(
    housingAddress: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onCreateLease: () -> Unit,
    onDelete: () -> Unit,
    isCompact: Boolean,
    modifier: Modifier = Modifier,
    onShowActions: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isCompact) {
            TextButton(onClick = onBack) { Text("←") }
            Text(
                text = housingAddress,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onShowActions) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Actions")
            }
        } else {
            TextButton(onClick = onBack) {
                Text("← Logements")
            }
            Text("›", color = MaterialTheme.colorScheme.outline)
            Text(
                text = housingAddress,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modifier")
            }
            Button(onClick = onCreateLease) {
                Icon(Icons.Outlined.NoteAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Créer un bail")
            }
            OutlinedButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Supprimer", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
