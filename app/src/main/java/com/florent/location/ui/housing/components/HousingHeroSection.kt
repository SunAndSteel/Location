package com.florent.location.ui.housing.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.florent.location.domain.model.Housing
import com.florent.location.domain.model.HousingSituation
import com.florent.location.domain.model.PebRating
import com.florent.location.ui.components.formatCurrency

@Composable
fun HousingHeroSection(
    housing: Housing,
    situation: HousingSituation?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusBadge(situation)
            Text(housing.address.fullString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold)
            Text(
                text = "${housing.address.zipCode} ${housing.address.city} · ${housing.buildingLabel ?: "Appartement"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DashedDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                MetricItem("LOYER HC", formatCurrency(housing.rentCents), Modifier.weight(1f))
                DividerPipe()
                MetricItem("CHARGES", formatCurrency(housing.chargesCents), Modifier.weight(1f))
                DividerPipe()
                MetricItem("DPE", housing.pebRating.takeIf { it != PebRating.UNKNOWN }?.name ?: "—", Modifier.weight(1f))
                DividerPipe()
                MetricItem("STATUT", if (situation == HousingSituation.OCCUPE) "Occupé" else "Libre", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatusBadge(situation: HousingSituation?) {
    val (badgeColor, badgeText) = when (situation) {
        HousingSituation.OCCUPE -> MaterialTheme.colorScheme.tertiaryContainer to "Loué · Bail actif"
        HousingSituation.LIBRE -> MaterialTheme.colorScheme.secondaryContainer to "Vacant"
        HousingSituation.DRAFT -> MaterialTheme.colorScheme.surfaceVariant to "Brouillon"
        null -> MaterialTheme.colorScheme.surfaceVariant to "Chargement…"
    }
    val transition = rememberInfiniteTransition(label = "badge")
    val pulse by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    Row(
        modifier = Modifier.background(badgeColor, MaterialTheme.shapes.small).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(if (situation == HousingSituation.OCCUPE) pulse else 1f)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
        )
        Text(text = badgeText, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun DashedDivider() {
    val color = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )
    }
}

@Composable
private fun DividerPipe() {
    Spacer(
        modifier = Modifier
            .height(34.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}
