package com.heard.heartdisease.data.repo

import android.content.Context
import com.heard.heartdisease.data.local.HeartDatabase
import com.heard.heartdisease.data.local.PredictionEntity
import kotlinx.coroutines.flow.Flow

class PredictionRepository(context: Context) {
    private val dao = HeartDatabase.getInstance(context).predictionDao()

    val history: Flow<List<PredictionEntity>> = dao.observeAll()

    suspend fun save(entity: PredictionEntity) = dao.insert(entity)
}
