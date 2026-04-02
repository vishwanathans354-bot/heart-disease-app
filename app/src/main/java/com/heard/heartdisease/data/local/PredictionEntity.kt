package com.heard.heartdisease.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val createdAt: Long,
    val diseaseLikelihood: Float,
    val riskTierName: String,
    val summary: String,
    val featuresJson: String,
    val scannedHeartRate: Int?
)
