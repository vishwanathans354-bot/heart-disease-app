package com.heard.heartdisease

import android.app.Application
import com.heard.heartdisease.data.local.HeartDatabase
import com.heard.heartdisease.data.ml.HeartRiskInterpreter

class HeartApplication : Application() {
    val database by lazy { HeartDatabase.getInstance(this) }
    val riskInterpreter by lazy { HeartRiskInterpreter(this) }
}
