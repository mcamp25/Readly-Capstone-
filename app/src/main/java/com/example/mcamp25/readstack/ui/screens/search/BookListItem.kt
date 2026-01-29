package com.example.mcamp25.readstack.ui.screens.search



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Precision
import com.example.mcamp25.readstack.R
import com.example.mcamp25.readstack.data.local.BookEntity
import com.example.mcamp25.readstack.data.network.BookItem
import com.example.mcamp25.readstack.data.network.getBestUrl
import com.example.mcamp25.readstack.ui.SharpenAndContrastTransformation
import com.example.mcamp25.readstack.ui.components.BookMetadata
import com.example.mcamp25.readstack.ui.components.RatingBarMini
import com.example.mcamp25.readstack.ui.components.StatusBadge
import com.example.mcamp25.readstack.ui.components.ShareAction
import com.example.mcamp25.readstack.ui.components.getHighlightedText
import com.example.mcamp25.readstack.ui.components.parseHtml
import com.example.mcamp25.readstack.ui.components.shimmerEffect
import com.example.mcamp25.readstack.ui.toHighResBookUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListItem(book: BookItem, query: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val highlightColor = MaterialTheme.colorScheme.secondary
    val highlightedTitle = remember(book.volumeInfo.title, query, highlightColor) {
        getHighlightedText(book.volumeInfo.title, query, highlightColor)
    }

    BaseBookCard(onClick = onClick, modifier = modifier) {
        BookCover(
            url = book.volumeInfo.imageLinks.getBestUrl(),
            title = book.volumeInfo.title,
            modifier = Modifier.width(80.dp).height(120.dp)
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) {
            Text(highlightedTitle, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author", style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            BookMetadata(book.volumeInfo.publishedDate, book.volumeInfo.pageCount ?: book.volumeInfo.printedPageCount)
            Text(book.volumeInfo.description.parseHtml(), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        
        ShareAction(title = book.volumeInfo.title, author = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalBookListItem(book: BookEntity, onClick: () -> Unit, onRatingChanged: (Int) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current

    BaseBookCard(onClick = onClick, modifier = modifier, elevation = 8.dp) {
        BookCover(
            url = book.thumbnail,
            title = book.title,
            modifier = Modifier.width(90.dp).height(130.dp),
            showGradient = true
        )
        
        Spacer(Modifier.width(16.dp))
        
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(book.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, false))
                if (book.isCurrentlyReading) StatusBadge(Icons.Default.AutoStories, MaterialTheme.colorScheme.secondary)
                if (book.isRead) StatusBadge(Icons.Default.Check, MaterialTheme.colorScheme.primary)
            }
            Text(book.author, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            RatingBarMini(rating = book.rating, onRatingChanged = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onRatingChanged(it) })
            BookMetadata(book.publishedDate, book.pageCount)
            Text(book.description.parseHtml(), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        
        ShareAction(title = book.title, author = book.author)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BaseBookCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = 2.dp,
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalRippleConfiguration provides RippleConfiguration(color = MaterialTheme.colorScheme.secondary)) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(elevation),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, content = content)
        }
    }
}

@Composable
private fun BookCover(url: String?, title: String, modifier: Modifier = Modifier, showGradient: Boolean = false) {
    val noCover = painterResource(id = R.drawable.no_cover)
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(if (url.isNullOrBlank() || url == "null") null else url.toHighResBookUrl())
            .transformations(SharpenAndContrastTransformation(contrast = 1.1f, sharpenAmount = 0.5f))
            .crossfade(true).allowHardware(false).precision(Precision.EXACT).build(),
        contentDescription = title,
        modifier = modifier.clip(RoundedCornerShape(8.dp)).drawWithContent {
            drawContent()
            if (showGradient) drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f))))
        },
        contentScale = ContentScale.Crop,
        filterQuality = FilterQuality.High
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> Box(Modifier.fillMaxSize().shimmerEffect())
            is AsyncImagePainter.State.Error, is AsyncImagePainter.State.Empty -> Image(noCover, null, Modifier.fillMaxSize(), contentScale = ContentScale.Inside)
            else -> SubcomposeAsyncImageContent()
        }
    }
}




