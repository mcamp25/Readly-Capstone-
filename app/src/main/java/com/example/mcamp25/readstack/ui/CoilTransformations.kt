package com.example.mcamp25.readstack.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import coil.size.Size
import coil.transform.Transformation
import androidx.core.graphics.createBitmap
@Suppress("unused")
class SimpleColorTransformation(
    val contrast: Float = 1.0f,
    val brightness: Float = 0f,
    val saturation: Float = 1.0f
) : Transformation {
    override val cacheKey: String = "SimpleColor_v1(c=$contrast,b=$brightness,s=$saturation)"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        return applyColorAdjustments(input)
    }

    private fun applyColorAdjustments(input: Bitmap) =
        createBitmap(input.width, input.height).also { output ->
            val t = (1.0f - contrast) / 2.0f * 255.0f
            val cm = ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, t + brightness,
                    0f, contrast, 0f, 0f, t + brightness,
                    0f, 0f, contrast, 0f, t + brightness,
                    0f, 0f, 0f, 1f, 0f
                )
            ).apply { postConcat(ColorMatrix().apply { setSaturation(saturation) }) }
            Canvas(output).drawBitmap(
                input,
                0f,
                0f,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(cm)
                })
        }
}


fun String.toHighResBookUrl() = Regex("(?:id=|frontcover/)([^&? /]+)").find(this)?.groupValues?.get(1)?.let {
    "https://books.google.com/books/content?id=$it&printsec=frontcover&img=1&zoom=1&source=gbs_api"
} ?: replace("http:", "https:").replace("&edge=curl", "")
