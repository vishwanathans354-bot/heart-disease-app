package com.heard.heartdisease.data.ml

import com.google.gson.annotations.SerializedName

data class PreprocessingConfig(
    @SerializedName("feature_order") val featureOrder: List<String>,
    @SerializedName("feature_indices") val featureIndices: List<Int>,
    @SerializedName("imputer_statistics") val imputerStatistics: List<Double>,
    @SerializedName("scaler_mean") val scalerMean: List<Double>,
    @SerializedName("scaler_scale") val scalerScale: List<Double>,
    @SerializedName("risk_thresholds") val riskThresholds: RiskThresholds,
    @SerializedName("dataset_note") val datasetNote: String
) {
    data class RiskThresholds(
        @SerializedName("low_max") val lowMax: Float,
        @SerializedName("high_min") val highMin: Float
    )
}
