package com.heard.heartdisease.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.heard.heartdisease.data.local.PredictionEntity
import com.heard.heartdisease.data.ml.HeartRiskInterpreter
import com.heard.heartdisease.data.repo.PredictionRepository
import com.heard.heartdisease.domain.HeartFeatures
import com.heard.heartdisease.domain.PredictionResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PredictionViewModel(
    application: Application,
    private val interpreter: HeartRiskInterpreter,
    private val repository: PredictionRepository
) : AndroidViewModel(application) {

    private val gson = Gson()

    val history: Flow<List<PredictionEntity>> = repository.history

    private val _result = MutableStateFlow<PredictionResult?>(null)
    val result: StateFlow<PredictionResult?> = _result

    private val _scannedHeartRate = MutableStateFlow<Int?>(null)
    val scannedHeartRate: StateFlow<Int?> = _scannedHeartRate

    fun setScannedHeartRate(bpm: Int?) {
        _scannedHeartRate.value = bpm
    }

    fun predict(features: HeartFeatures) {
        try {
            val res = interpreter.predict(features)
            _result.value = res

            viewModelScope.launch {
                repository.save(
                    PredictionEntity(
                        createdAt = System.currentTimeMillis(),
                        diseaseLikelihood = res.diseaseLikelihood,
                        riskTierName = res.riskTier.name,
                        summary = res.binarySummary,
                        featuresJson = gson.toJson(features),
                        scannedHeartRate = _scannedHeartRate.value
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _result.value = null
        }
    }
}