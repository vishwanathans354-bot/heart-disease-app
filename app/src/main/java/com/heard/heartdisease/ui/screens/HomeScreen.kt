package com.heard.heartdisease.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heard.heartdisease.ui.components.DisclaimerBanner
import com.heard.heartdisease.ui.viewmodel.PredictionViewModel

@Composable
fun HomeScreen(
    vm: PredictionViewModel,
    onPredict: () -> Unit,
    onHistory: () -> Unit
) {
    val history by vm.history.collectAsStateWithLifecycle(initialValue = emptyList())
    val latest = history.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        DisclaimerBanner()

        AnimatedVisibility(visible = latest != null, enter = fadeIn()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Latest assessment", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "Risk tier: ${latest?.riskTierName ?: "—"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Disease likelihood (model): ${((latest?.diseaseLikelihood ?: 0f) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        if (latest == null) {
            Text(
                text = "Run a prediction to see your risk summary. All processing stays on your device.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onPredict,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Text("New prediction", modifier = Modifier.padding(start = 8.dp))
        }

        OutlinedButton(
            onClick = onHistory,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.History, contentDescription = null)
            Text("History", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
