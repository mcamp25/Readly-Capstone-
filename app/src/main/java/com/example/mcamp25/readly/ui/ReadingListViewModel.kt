package com.example.mcamp25.readly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mcamp25.readly.ReadlyApplication
import com.example.mcamp25.readly.data.BookDao
import com.example.mcamp25.readly.data.BookEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadingListViewModel(private val bookDao: BookDao) : ViewModel() {

    val readingList: StateFlow<List<BookEntity>> = bookDao.getAllBooks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeFromReadingList(book: BookEntity) {
        viewModelScope.launch {
            bookDao.deleteBook(book)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ReadlyApplication)
                ReadingListViewModel(application.database.bookDao())
            }
        }
    }
}
