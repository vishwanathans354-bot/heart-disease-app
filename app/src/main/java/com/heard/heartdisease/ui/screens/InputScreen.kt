package com.heard.heartdisease.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heard.heartdisease.domain.HeartFeatures
import com.heard.heartdisease.ui.components.DisclaimerBanner
import com.heard.heartdisease.ui.viewmodel.PredictionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    vm: PredictionViewModel,
    onBack: () -> Unit,
    onScan: () -> Unit,
    onResult: () -> Unit
) {
    var age by remember { mutableFloatStateOf(54f) }
    var sex by remember { mutableIntStateOf(1) }
    var cp by remember { mutableIntStateOf(1) }
    var trestbps by remember { mutableFloatStateOf(130f) }
    var chol by remember { mutableFloatStateOf(240f) }
    var fbs by remember { mutableIntStateOf(0) }
    var restecg by remember { mutableIntStateOf(0) }
    var thalach by remember { mutableFloatStateOf(150f) }
    var exang by remember { mutableIntStateOf(0) }
    var oldpeak by remember { mutableFloatStateOf(1f) }
    var slope by remember { mutableIntStateOf(2) }
    var ca by remember { mutableIntStateOf(0) }
    var thal by remember { mutableIntStateOf(3) }

    var error by remember { mutableStateOf<String?>(null) }
    val scanned by vm.scannedHeartRate.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinical inputs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (scanned != null) thalach = scanned!!.toFloat()
                    }, enabled = scanned != null) {
                        Text("Use HR ${scanned ?: "—"}")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DisclaimerBanner()
            Text(
                "Enter values matching UCI Cleveland encodings. Optional: measure pulse with the camera for max HR.",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Age: ${age.toInt()}")
                Slider(age, { age = it }, valueRange = 18f..100f)
            }

            RowChipRow("Sex", listOf("Female (0)" to 0, "Male (1)" to 1), sex) { sex = it }

            RowChipRow(
                "Chest pain type",
                listOf(
                    "Typical angina (1)" to 1,
                    "Atypical (2)" to 2,
                    "Non-anginal (3)" to 3,
                    "Asymptomatic (4)" to 4
                ),
                cp
            ) { cp = it }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Resting BP", modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = trestbps.toInt().toString(),
                    onValueChange = { trestbps = it.toFloatOrNull() ?: trestbps },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    suffix = { Text("mm Hg") }
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Cholesterol", modifier = Modifier.weight(1f))
                OutlinedTextField(
                    value = chol.toInt().toString(),
                    onValueChange = { chol = it.toFloatOrNull() ?: chol },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    suffix = { Text("mg/dl") }
                )
            }

            RowChipRow("Fasting BS >120", listOf("No (0)" to 0, "Yes (1)" to 1), fbs) { fbs = it }

            RowChipRow(
                "Resting ECG",
                listOf("Normal (0)" to 0, "ST-T abnormality (1)" to 1, "LV hypertrophy (2)" to 2),
                restecg
            ) { restecg = it }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Max heart rate")
                    Text(
                        "Camera estimate: ${scanned ?: "—"} BPM",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Button(onClick = onScan) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Text("Scan", modifier = Modifier.padding(start = 6.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${thalach.toInt()} bpm")
                Slider(thalach, { thalach = it }, valueRange = 60f..210f)
            }

            RowChipRow("Exercise angina", listOf("No (0)" to 0, "Yes (1)" to 1), exang) { exang = it }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ST depression (oldpeak)")
                Slider(oldpeak, { oldpeak = it }, valueRange = 0f..6f)
            }

            RowChipRow(
                "ST slope",
                listOf("Upsloping (1)" to 1, "Flat (2)" to 2, "Downsloping (3)" to 3),
                slope
            ) { slope = it }

            RowChipRow(
                "Major vessels (ca)",
                listOf("0" to 0, "1" to 1, "2" to 2, "3" to 3),
                ca
            ) { ca = it }

            RowChipRow(
                "Thal",
                listOf("Normal (3)" to 3, "Fixed defect (6)" to 6, "Reversible (7)" to 7),
                thal
            ) { thal = it }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val v = validate(
                        age, sex, cp, trestbps, chol, fbs, restecg, thalach,
                        exang, oldpeak, slope, ca, thal
                    )
                    if (v.isFailure) {
                        error = v.exceptionOrNull()?.message
                    } else {
                        error = null
                        vm.predict(v.getOrThrow())
                        onResult()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run model")
            }
        }
    }

    error?.let { msg ->
        AlertDialog(
            onDismissRequest = { error = null },
            confirmButton = { TextButton(onClick = { error = null }) { Text("OK") } },
            title = { Text("Check inputs") },
            text = { Text(msg) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowChipRow(
    label: String,
    options: List<Pair<String, Int>>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { (text, value) ->
                FilterChip(
                    selected = selected == value,
                    onClick = { onSelect(value) },
                    label = { Text(text) }
                )
            }
        }
    }
}

private fun validate(
    age: Float,
    sex: Int,
    cp: Int,
    trestbps: Float,
    chol: Float,
    fbs: Int,
    restecg: Int,
    thalach: Float,
    exang: Int,
    oldpeak: Float,
    slope: Int,
    ca: Int,
    thal: Int
): Result<HeartFeatures> {
    if (age !in 18f..120f) return Result.failure(IllegalArgumentException("Age must be 18–120"))
    if (sex !in 0..1) return Result.failure(IllegalArgumentException("Invalid sex"))
    if (cp !in 1..4) return Result.failure(IllegalArgumentException("Chest pain must be 1–4"))
    if (trestbps !in 80f..220f) return Result.failure(IllegalArgumentException("BP out of range"))
    if (chol !in 100f..600f) return Result.failure(IllegalArgumentException("Cholesterol out of range"))
    if (fbs !in 0..1) return Result.failure(IllegalArgumentException("Invalid fbs"))
    if (restecg !in 0..2) return Result.failure(IllegalArgumentException("Invalid restecg"))
    if (thalach !in 60f..220f) return Result.failure(IllegalArgumentException("Max HR out of range"))
    if (exang !in 0..1) return Result.failure(IllegalArgumentException("Invalid exang"))
    if (oldpeak !in 0f..10f) return Result.failure(IllegalArgumentException("Oldpeak out of range"))
    if (slope !in 1..3) return Result.failure(IllegalArgumentException("Invalid slope"))
    if (ca !in 0..3) return Result.failure(IllegalArgumentException("Invalid ca"))
    if (thal !in listOf(3, 6, 7)) return Result.failure(IllegalArgumentException("Invalid thal"))

    return Result.success(
        HeartFeatures(
            age = age,
            sex = sex,
            cp = cp,
            trestbps = trestbps,
            chol = chol,
            fbs = fbs,
            restecg = restecg,
            thalach = thalach,
            exang = exang,
            oldpeak = oldpeak,
            slope = slope,
            ca = ca,
            thal = thal
        )
    )
}
