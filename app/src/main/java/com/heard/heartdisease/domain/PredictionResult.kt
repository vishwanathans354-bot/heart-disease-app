package com.heard.heartdisease.domain

/**
 * [diseaseLikelihood] is P(positive class) from the binary UCI-aligned model (angiographic disease).
 * This is not a specific disease subtype — see [datasetNote].
 */
data class PredictionResult(
    val diseaseLikelihood: Float,
    val riskTier: RiskTier,
    val confidencePercent: Int,
    val binarySummary: String,
    val datasetNote: String
)
