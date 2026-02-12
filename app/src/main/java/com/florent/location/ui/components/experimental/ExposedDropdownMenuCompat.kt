package com.florent.location.ui.components.experimental

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Wrapper de compatibilité temporaire autour de `DropdownMenu`.
 *
 * Statut: conservé pour faciliter une éventuelle réintroduction de `ExposedDropdownMenu`
 * Material3 sans impacter les écrans métier.
 */
@Composable
fun ExposedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        content = content
    )
}
