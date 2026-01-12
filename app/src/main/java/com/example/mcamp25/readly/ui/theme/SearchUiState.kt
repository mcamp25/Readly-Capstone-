package com.example.mcamp25.readly.ui.theme

import com.example.mcamp25.readly.data.BookItem

sealed interface SearchUiState {
    object Idle : SearchUiState
    object Loading : SearchUiState
    data class Success(val books: List<BookItem>) : SearchUiState
    object Error : SearchUiState
}
