package com.heard.heartdisease.domain

enum class RiskTier {
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun fromProbability(p: Float, lowMax: Float, highMin: Float): RiskTier = when {
            p < lowMax -> LOW
            p >= highMin -> HIGH
            else -> MEDIUM
        }
    }
}
