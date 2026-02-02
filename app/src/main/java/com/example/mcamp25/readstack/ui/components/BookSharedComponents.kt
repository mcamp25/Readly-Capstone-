package com.example.mcamp25.readstack.ui.components

import android.content.Intent
import android.text.Html
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadge(icon: ImageVector, color: Color,label: String) {
    Surface(shape = CircleShape, color = color, modifier = Modifier.padding(start = 8.dp).size(26.dp), shadowElevation = 2.dp) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, label, tint = Color.White, modifier = Modifier.size(18.dp)) }
    }
}

@Composable
fun BookMetadata(date: String?, pages: Int?) {
    Row(Modifier.padding(vertical = 2.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        date?.take(4)?.let { year -> MetaChip(Icons.Default.CalendarMonth, year) }
        pages?.takeIf { it > 0 }?.let { count -> MetaChip(Icons.AutoMirrored.Filled.MenuBook, "$count p") }
    }
}

@Composable
fun MetaChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun RatingBarMini(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(
        modifier = Modifier.semantics {
            contentDescription = "Current rating: $rating out of 5 stars"
        }
    ) {
        for (i in 1..5) {
            val starSize by animateDpAsState(
                targetValue = if (i <= rating) 28.dp else 24.dp,
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                label = "starSize"
            )
            Icon(
                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) MaterialTheme.colorScheme.secondary else Color.Gray,
                modifier = Modifier
                    .size(starSize)
                    .semantics{
                        contentDescription = "Rate $i stars"
                    }
                    .clickable(
                        onClickLabel = "Rate $i stars",
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onRatingChanged(i) }
            )
        }
    }
}

@Composable
fun ShareAction(title: String, author: String) {
    val context = LocalContext.current
    IconButton(onClick = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out this book: $title by $author")
        }
        context.startActivity(Intent.createChooser(intent, null))
    }, modifier = Modifier.wrapContentHeight(Alignment.Top)) {
        Icon(Icons.Default.Share, "Share", tint = MaterialTheme.colorScheme.primary)
    }
}

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
