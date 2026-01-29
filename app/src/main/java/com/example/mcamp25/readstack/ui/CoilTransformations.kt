package com.example.mcamp25.readstack.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import coil.size.Size
import coil.transform.Transformation
import androidx.core.graphics.createBitmap
import kotlin.math.abs

class SharpenAndContrastTransformation(
    private val contrast: Float = 1.0f,
    private val brightness: Float = 0f,
    private val saturation: Float = 1.0f,
    private val sharpenAmount: Float = 0.7f,
    private val threshold: Int = 15
) : Transformation {

    override val cacheKey: String = "SharpenAndContrast_v18(c=$contrast,b=$brightness,s=$saturation,a=$sharpenAmount,t=$threshold)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val (w, h) = input.width to input.height
        val pixels = IntArray(w * h).apply { input.getPixels(this, 0, w, 0, 0, w, h) }
        val result = pixels.copyOf()
        val offsets = intArrayOf(-w -1, -w, -w + 1, -1, 1, w - 1, w, w + 1)



        for (y in 1 until h - 1) {
            val yOff = y * w
            for (x in 1 until w - 1) {
                val idx = yOff + x
                val center = pixels[idx]
                var sR = 0f; var sG = 0f; var sB = 0f
                offsets.forEach { o ->
                    val p = pixels[idx + o]
                    sR += p.r(); sG += p.g(); sB += p.b()
                }

                val dR = center.r() - (sR / 8f); val dG = center.g() - (sG / 8f); val dB = center.b() - (sB / 8f)

                val factor = calculateFactor(abs(dR) + abs(dG) + abs(dB), center.luma(), threshold, sharpenAmount)

                result[idx] = packRgb(
                    (center.r() + factor * dR).toInt().coerceIn(0, 255),
                    (center.g() + factor * dG).toInt().coerceIn(0, 255),
                    (center.b() + factor * dB).toInt().coerceIn(0, 255)
                )
            }
        }


        val sharpened = Bitmap.createBitmap(result, w, h, Bitmap.Config.ARGB_8888)
        return applyColorAdjustments(sharpened).also { sharpened.recycle() }
    }

    private fun applyColorAdjustments(input: Bitmap) = createBitmap(input.width, input.height).also { output ->
        val t = (1.0f - contrast) / 2.0f * 255.0f
        val cm = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, t + brightness,
            0f, contrast, 0f, 0f, t + brightness,
            0f, 0f, contrast, 0f, t + brightness,
            0f, 0f, 0f, 1f, 0f
        )).apply { postConcat(ColorMatrix().apply { setSaturation(saturation) }) }

        Canvas(output).drawBitmap(input, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(cm)
        })
    }
}

private fun Int.r() = (this shr 16) and 0xFF
private fun Int.g() = (this shr 8) and 0xFF
private fun Int.b() = this and 0xFF
private fun Int.luma() = (r() * 0.299f + g() * 0.587f + b() * 0.114f) / 255f
private fun packRgb(r: Int, g: Int, b: Int) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b

private fun calculateFactor(v: Float, l: Float, t: Int, a: Float) =
    ((1f - l * l).coerceIn(0.2f, 1.0f)).let {  p -> if (v > t) ((v - t) / 20f).coerceIn(0f, 1f) * a * p else -0.1f }



fun String.toHighResBookUrl() = Regex("(?:id=|frontcover/)([^&? /]+)").find(this)?.groupValues?.get(1)?.let {
    "https://books.google.com/books/content?id=$it&printsec=frontcover&img=1&zoom=1&source=gbs_api"
} ?: replace("http:", "https:").replace("&edge=curl", "")
