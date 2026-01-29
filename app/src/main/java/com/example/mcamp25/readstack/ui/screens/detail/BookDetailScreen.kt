package com.example.mcamp25.readstack.ui.screens.detail

import android.content.Context
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.example.mcamp25.readstack.ui.screens.detail.components.BlurredHeader
import com.example.mcamp25.readstack.ui.screens.detail.components.DetailTopBar
import com.example.mcamp25.readstack.ui.screens.detail.components.ErrorState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    initialPages: Int? = null,
    initialDate: String? = null,
    vm: BookDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val details = vm.details
    val currentRating = vm.currentRating
    val isRead = vm.isRead
    val inProgress = vm.inProgress
    val haptic = LocalHapticFeedback.current
   val vibrator = rememberVibrator()

    //Re-fetch if the ID changes
    LaunchedEffect(bookId) {
        vm.getBook(bookId)
    }
// Success, Loading and Error States
    Box(modifier = Modifier
        .fillMaxSize()
        .navigationBarsPadding()) {
        

        Box(modifier = Modifier.fillMaxSize()) {
            when (details) {
                is BookDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                is BookDetailUiState.Success -> {
                    // How the user felt about the story and their book progress
                    SuccessContent(
                        book = details.book,
                        bookId = bookId,
                        currentRating = currentRating,
                        isRead = isRead,
                        inProgress = inProgress,
                        initialPages = initialPages,
                        initialDate = initialDate,
                        vm = vm,
                        haptic = haptic,
                        vibrator = vibrator
                    )
                }
                is BookDetailUiState.Error -> {
                    ErrorState(onRetry = { vm.getBook(bookId) })
                }
            }
        }

        BlurredHeader()

        val displayTitle = (details as? BookDetailUiState.Success)?.book?.volumeInfo?.title
        DetailTopBar(title = displayTitle, onNavigateBack = onNavigateBack)
    }
}


@Composable
private fun rememberVibrator(): Vibrator {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
}
