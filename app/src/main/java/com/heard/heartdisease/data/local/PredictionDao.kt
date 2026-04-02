package com.heard.heartdisease.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    @Query("SELECT * FROM predictions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PredictionEntity>>

    @Insert
    suspend fun insert(entity: PredictionEntity)
}
