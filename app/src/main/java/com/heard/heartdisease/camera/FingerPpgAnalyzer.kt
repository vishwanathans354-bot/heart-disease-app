package com.heard.heartdisease.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Estimates heart rate (BPM) from finger-on-lens PPG using average Y-channel brightness.
 * **Not** disease detection — only approximate pulse rate for optional "max HR" input.
 */
class FingerPpgAnalyzer(
    private val onSample: (bpm: Int?, progress: Float) -> Unit
) : ImageAnalysis.Analyzer {

    private val timestamps = mutableListOf<Long>()
    private val values = mutableListOf<Double>()
    private val maxSamples = 400
    private var startNs: Long = 0L

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val plane = image.planes.firstOrNull()
        val buffer = plane?.buffer
        val w = image.width
        val h = image.height
        val rowStride = plane?.rowStride ?: w
        val now = System.nanoTime()
        if (startNs == 0L) startNs = now

        var sum = 0.0
        var count = 0
        if (buffer != null) {
            val rw = w / 3
            val rh = h / 3
            val x0 = (w - rw) / 2
            val y0 = (h - rh) / 2
            for (yy in y0 until y0 + rh) {
                var pos = yy * rowStride + x0
                for (xx in 0 until rw) {
                    val idx = pos + xx
                    if (idx < buffer.capacity()) {
                        val v = buffer.get(idx).toInt() and 0xFF
                        sum += v
                        count++
                    }
                }
            }
        }
        val mean = if (count > 0) sum / count else 0.0

        timestamps.add(now)
        values.add(mean)
        while (timestamps.size > maxSamples) {
            timestamps.removeAt(0)
            values.removeAt(0)
        }

        val elapsedSec = (now - startNs) / 1_000_000_000f
        val progress = min(1f, elapsedSec / SAMPLE_SECONDS)
        val bpm = if (elapsedSec >= SAMPLE_SECONDS && values.size >= 60) estimateBpm() else null
        onSample(bpm, progress)

        image.close()
    }

    private fun estimateBpm(): Int? {
        val arr = values.toDoubleArray()
        if (arr.size < 30) return null
        val win = 15
        val detrended = DoubleArray(arr.size)
        for (i in arr.indices) {
            var s = 0.0
            var c = 0
            for (j in max(0, i - win / 2)..min(arr.lastIndex, i + win / 2)) {
                s += arr[j]
                c++
            }
            detrended[i] = arr[i] - s / c
        }
        val minDistNs = (0.45 * 1_000_000_000).toLong()
        var peaks = 0
        var lastPeak = Long.MIN_VALUE
        val sigma = std(detrended)
        val thresh = max(0.2, 0.15 * sigma)
        for (i in 2 until detrended.size - 2) {
            val v = detrended[i]
            if (v > detrended[i - 1] && v > detrended[i + 1] && v > thresh) {
                val t = timestamps[i]
                if (t - lastPeak >= minDistNs) {
                    peaks++
                    lastPeak = t
                }
            }
        }
        val durationSec = (timestamps.last() - timestamps.first()) / 1_000_000_000.0
        if (durationSec < 1.0) return null
        val bpm = (peaks / durationSec * 60.0).toInt()
        return bpm.coerceIn(40, 200)
    }

    private fun std(a: DoubleArray): Double {
        val m = a.average()
        var s = 0.0
        for (v in a) s += (v - m) * (v - m)
        return sqrt(s / a.size)
    }

    companion object {
        const val SAMPLE_SECONDS = 12f
    }
}
