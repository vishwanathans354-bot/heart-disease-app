package com.heard.heartdisease.data.ml

import android.content.Context
import com.google.gson.Gson
import com.heard.heartdisease.domain.HeartFeatures
import com.heard.heartdisease.domain.PredictionResult
import com.heard.heartdisease.domain.RiskTier
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

/**
 * Loads [preprocessing_config.json] and [heart_risk_model.tflite] from assets.
 * Inference runs fully on-device (no network).
 */
class HeartRiskInterpreter(context: Context) {

    private val appContext = context.applicationContext
    private val gson = Gson()

    private val config: PreprocessingConfig by lazy {
        appContext.assets.open(CONFIG_NAME).bufferedReader().use { reader ->
            gson.fromJson(reader, PreprocessingConfig::class.java)
        }
    }

    private val interpreter: Interpreter? by lazy {
        try {
            val model = loadModelFile(MODEL_NAME)
            Interpreter(model, Interpreter.Options().setNumThreads(4))
        } catch (_: Exception) {
            null
        }
    }

    fun predict(features: HeartFeatures): PredictionResult {
        val raw = features.toRawArray()
        require(raw.size == N_FEATURES) { "Expected $N_FEATURES raw inputs" }

        val scaled = FloatArray(N_FEATURES) { i ->
            val x = raw[i].toDouble()
            val xImp = if (x.isNaN()) config.imputerStatistics[i] else x
            ((xImp - config.scalerMean[i]) / config.scalerScale[i]).toFloat()
        }

        val inputDim = config.featureIndices.size
        val modelInput = FloatArray(inputDim) { j ->
            scaled[config.featureIndices[j]]
        }

        val p = interpreter?.let { interp ->
            val inputBuffer = floatArrayToBuffer(modelInput)
            val outputBuffer = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder())
            interp.run(inputBuffer, outputBuffer)
            outputBuffer.rewind()
            outputBuffer.float.coerceIn(0f, 1f)
        } ?: heuristicProbability(modelInput)
        val low = config.riskThresholds.lowMax
        val high = config.riskThresholds.highMin
        val tier = RiskTier.fromProbability(p, low, high)
        val confidence = (kotlin.math.max(p, 1f - p) * 100f).toInt()
        val summary = if (p >= 0.5f) {
            "Model suggests elevated likelihood of angiographic disease (UCI label)"
        } else {
            "Model suggests lower likelihood of angiographic disease (UCI label)"
        }

        return PredictionResult(
            diseaseLikelihood = p,
            riskTier = tier,
            confidencePercent = confidence,
            binarySummary = summary,
            datasetNote = config.datasetNote
        )
    }

    fun close() {
        interpreter?.close()
    }

    /** Used when [MODEL_NAME] is not bundled (e.g. dev build); rough demo score only. */
    private fun heuristicProbability(modelInput: FloatArray): Float {
        if (modelInput.isEmpty()) return 0.5f
        var s = 0f
        for (x in modelInput) s += x
        val z = s / modelInput.size
        val p = (1.0 / (1.0 + kotlin.math.exp(-z.toDouble()))).toFloat()
        return p.coerceIn(0.05f, 0.95f)
    }

    private fun loadModelFile(assetName: String): MappedByteBuffer {
        val fd = appContext.assets.openFd(assetName)
        FileInputStream(fd.fileDescriptor).use { fis ->
            fis.channel.use { channel ->
                return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
        }
    }

    private fun floatArrayToBuffer(data: FloatArray): ByteBuffer {
        val bb = ByteBuffer.allocateDirect(data.size * 4).order(ByteOrder.nativeOrder())
        for (f in data) bb.putFloat(f)
        bb.rewind()
        return bb
    }


    companion object {
        private const val CONFIG_NAME = "preprocessing_config.json"
        private const val MODEL_NAME = "heart_risk_model.tflite"
        private const val N_FEATURES = 13
    }
}
