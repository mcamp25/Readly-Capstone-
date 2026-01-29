package com.example.mcamp25.readstack.ui.utils

import android.text.Html
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun String?.parseHtml(): String = this?.let { Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT).toString() } ?: ""

fun getHighlightedText(text: String, query: String, color: Color): AnnotatedString = buildAnnotatedString {
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var start = 0
    while (start < text.length) {
        val index = lowerText.indexOf(lowerQuery, start)
        if (index == -1 || lowerQuery.isBlank()) {
            append(text.substring(start))
            break
        }
        append(text.substring(start, index))
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.Black)) { append(text.substring(index, index + query.length)) }
        start = index + query.length
    }
}