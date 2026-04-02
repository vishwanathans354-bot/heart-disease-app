package com.heard.heartdisease.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.heard.heartdisease.domain.PredictionResult
import com.heard.heartdisease.domain.RiskTier
import com.heard.heartdisease.ui.components.DisclaimerBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: PredictionResult,
    onBackHome: () -> Unit
) {
    val p = result.diseaseLikelihood
    val animated by animateFloatAsState(
        targetValue = p,
        animationSpec = tween(700),
        label = "prob"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assessment") },
                navigationIcon = {
                    IconButton(onClick = onBackHome) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DisclaimerBanner()
            Text(
                text = "Risk tier: ${result.riskTier.name}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Confidence (decision): ${result.confidencePercent}%",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Estimated likelihood of angiographic disease (UCI label): ${(p * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge
            )

            LinearProgressIndicator(
                progress = animated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
            )

            RiskBarChart(tier = result.riskTier, probability = animated)

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp)) {
                    Text("Summary", style = MaterialTheme.typography.labelLarge)
                    Text(result.binarySummary, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Why not a specific disease class?", style = MaterialTheme.typography.titleSmall)
                    Text(
                        result.datasetNote,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "The model below is trained for binary risk. Clinical categories for reference only:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    val classes = listOf(
                        "0 — Normal (no disease)",
                        "1 — Coronary artery disease (CAD)",
                        "2 — Myocardial infarction",
                        "3 — Arrhythmia",
                        "4 — Heart failure",
                        "5 — Cardiomyopathy"
                    )
                    classes.forEach { line ->
                        Text(line, style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "These labels are not predicted by this dataset-backed model; consult a cardiologist for diagnosis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskBarChart(tier: RiskTier, probability: Float) {
    val low = 0.35f
    val high = 0.65f
    val tierColor = when (tier) {
        RiskTier.LOW -> Color(0xFF2E7D32)
        RiskTier.MEDIUM -> Color(0xFFF9A825)
        RiskTier.HIGH -> Color(0xFFC62828)
    }
    Column {
        Text("Risk visualization", style = MaterialTheme.typography.labelLarge)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                color = Color(0x332E7D32),
                size = Size(w * low, h),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0x4D2196F3),
                topLeft = androidx.compose.ui.geometry.Offset(w * low, 0f),
                size = Size(w * (high - low), h),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = Color(0x33C62828),
                topLeft = androidx.compose.ui.geometry.Offset(w * high, 0f),
                size = Size(w * (1f - high), h),
                cornerRadius = CornerRadius(8f, 8f)
            )
            val marker = (probability * w).coerceIn(4f, w - 4f)
            drawCircle(color = tierColor, radius = 10f, center = androidx.compose.ui.geometry.Offset(marker, h / 2f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Low", style = MaterialTheme.typography.labelLarge, color = Color(0xFF2E7D32))
            Text("Medium", style = MaterialTheme.typography.labelLarge)
            Text("High", style = MaterialTheme.typography.labelLarge, color = Color(0xFFC62828))
        }
    }
}
