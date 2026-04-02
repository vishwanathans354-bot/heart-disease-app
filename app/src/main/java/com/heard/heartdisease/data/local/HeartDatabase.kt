package com.heard.heartdisease.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PredictionEntity::class], version = 1, exportSchema = false)
abstract class HeartDatabase : RoomDatabase() {
    abstract fun predictionDao(): PredictionDao

    companion object {
        @Volatile
        private var INSTANCE: HeartDatabase? = null

        fun getInstance(context: Context): HeartDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HeartDatabase::class.java,
                    "heart_risk.db"
                ).build().also { INSTANCE = it }
            }
    }
}
