package com.heard.heartdisease.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.heard.heartdisease.data.ml.HeartRiskInterpreter
import com.heard.heartdisease.data.repo.PredictionRepository

class VmFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PredictionViewModel::class.java)) {

            val interpreter = HeartRiskInterpreter(application)
            val repository = PredictionRepository(application)

            return PredictionViewModel(application, interpreter, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}