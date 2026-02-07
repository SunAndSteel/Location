package com.florent.location.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthSize {
    Compact,
    Medium,
    Expanded
}

fun windowWidthSize(maxWidth: Dp): WindowWidthSize {
    return when {
        maxWidth < 600.dp -> WindowWidthSize.Compact
        maxWidth < 840.dp -> WindowWidthSize.Medium
        else -> WindowWidthSize.Expanded
    }
}

@Composable
fun AdaptiveContent(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    contentMaxWidth: Dp = UiTokens.ContentMaxWidthMedium,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        val horizontalPadding = when (windowWidthSize(maxWidth)) {
            WindowWidthSize.Compact -> UiTokens.SpacingL
            WindowWidthSize.Medium -> UiTokens.SpacingXL
            WindowWidthSize.Expanded -> UiTokens.SpacingXXL
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = contentMaxWidth)
                    .padding(horizontal = horizontalPadding, vertical = UiTokens.SpacingL),
                content = content
            )
        }
    }
}

@Composable
fun EmptyDetailPane(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
