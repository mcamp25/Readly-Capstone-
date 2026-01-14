package com.example.mcamp25.readly.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mcamp25.readly.ReadlyApplication
import com.example.mcamp25.readly.data.BookDao
import com.example.mcamp25.readly.data.BookEntity
import com.example.mcamp25.readly.data.BookItem
import com.example.mcamp25.readly.data.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface BookDetailUiState {
    object Loading : BookDetailUiState
    data class Success(val book: BookItem) : BookDetailUiState
    object Error : BookDetailUiState
}

class BookDetailViewModel(private val bookDao: BookDao) : ViewModel() {
    var uiState: BookDetailUiState by mutableStateOf(BookDetailUiState.Loading)
        private set

    fun getBook(id: String) {
        viewModelScope.launch {
            uiState = BookDetailUiState.Loading
            uiState = try {
                val book = RetrofitClient.apiService.getBook(id)
                BookDetailUiState.Success(book)
            } catch (_: IOException) {
                BookDetailUiState.Error
            } catch (_: Exception) {
                BookDetailUiState.Error
            }
        }
    }

    fun getRating(bookId: String): Flow<Int> = bookDao.getRating(bookId)

    fun updateRating(bookId: String, rating: Int) {
        viewModelScope.launch {
            bookDao.updateRating(bookId, rating)
        }
    }

    fun addToReadingList(book: BookItem) {
        viewModelScope.launch {
            val entity = BookEntity(
                id = book.id,
                title = book.volumeInfo.title,
                author = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Author",
                description = book.volumeInfo.description ?: "",
                thumbnail = book.volumeInfo.imageLinks?.thumbnailUrl?.replace("http:", "https:") ?: ""
            )
            bookDao.insert(entity)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ReadlyApplication)
                BookDetailViewModel(application.database.bookDao())
            }
        }
    }
}
