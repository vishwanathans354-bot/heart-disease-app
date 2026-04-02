package com.heard.heartdisease.domain

/**
 * UCI Cleveland processed attributes (13 features). Values must match training encodings.
 */
data class HeartFeatures(
    val age: Float,
    val sex: Int, // 0 female, 1 male
    val cp: Int, // 1–4 chest pain type
    val trestbps: Float, // resting BP mm Hg
    val chol: Float, // cholesterol mg/dl
    val fbs: Int, // fasting blood sugar >120: 0/1
    val restecg: Int, // 0,1,2
    val thalach: Float, // max HR
    val exang: Int, // exercise angina 0/1
    val oldpeak: Float,
    val slope: Int, // 1,2,3
    val ca: Int, // 0–3
    val thal: Int // 3, 6, 7
) {
    fun toRawArray(): FloatArray = floatArrayOf(
        age, sex.toFloat(), cp.toFloat(), trestbps, chol, fbs.toFloat(),
        restecg.toFloat(), thalach, exang.toFloat(), oldpeak,
        slope.toFloat(), ca.toFloat(), thal.toFloat()
    )
}
