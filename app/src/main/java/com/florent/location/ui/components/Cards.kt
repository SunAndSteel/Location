@file:OptIn(ExperimentalLayoutApi::class)

package com.florent.location.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

enum class CardVariant {
    Default,
    Highlighted,
    Warning
}

@Composable
fun variantCardColors(variant: CardVariant) = when (variant) {
    CardVariant.Default -> CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
    CardVariant.Highlighted -> CardDefaults.filledCardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    )
    CardVariant.Warning -> CardDefaults.filledCardColors(
        containerColor = MaterialTheme.colorScheme.errorContainer
    )
}

@Composable
fun DefaultCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = variantCardColors(CardVariant.Default),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.medium,
        content = content
    )
}

@Composable
fun HighlightCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FilledCard(
        modifier = modifier,
        colors = variantCardColors(CardVariant.Highlighted),
        elevation = CardDefaults.filledCardElevation(
            defaultElevation = 0.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 1.dp
        ),
        shape = MaterialTheme.shapes.medium,
        content = content
    )
}

@Composable
fun WarningCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    FilledCard(
        modifier = modifier,
        colors = variantCardColors(CardVariant.Warning),
        elevation = CardDefaults.filledCardElevation(
            defaultElevation = 0.dp,
            focusedElevation = 1.dp,
            hoveredElevation = 1.dp
        ),
        shape = MaterialTheme.shapes.medium,
        content = content
    )
}

@Composable
fun DetailChipRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
fun HeroCard(
    title: String,
    heroValue: String,
    heroLabel: String,
    modifier: Modifier = Modifier,
    status: String? = null,
    variant: CardVariant = CardVariant.Default,
    facts: List<Pair<String, String>> = emptyList()
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = variantCardColors(variant),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp,
            focusedElevation = 2.dp,
            hoveredElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                status?.let { StatusBadge(text = it) }
            }
            HeroMetric(value = heroValue, label = heroLabel)
            if (facts.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingXs)) {
                    facts.forEach { (label, value) ->
                        LabeledValueRow(label = label, value = value)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    tonalColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentPadding: PaddingValues = PaddingValues(UiTokens.SpacingL),
    tonalElevation: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tonalColor,
        tonalElevation = tonalElevation
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(UiTokens.SpacingS),
            content = content
        )
    }
}

@Composable
fun ResultCard(
    title: String,
    entries: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        tonalColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        entries.forEach { (label, value) ->
            LabeledValueRow(label = label, value = value)
        }
    }
}

@Composable
fun TimelineListItem(
    title: String,
    subtitle: String,
    trailing: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(UiTokens.SpacingM),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            trailing?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
