package com.example.mcamp25.readly.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mcamp25.readly.data.RetrofitClient
import com.example.mcamp25.readly.ui.theme.SearchUiState
import kotlinx.coroutines.launch
import java.io.IOException

class SearchViewModel : ViewModel() {
    var searchUiState: SearchUiState by mutableStateOf(SearchUiState.Idle)
        private set

    fun searchBooks(query: String ) {
        if (query.isBlank()) return
        
        viewModelScope.launch {
            searchUiState = SearchUiState.Loading
            searchUiState = try {
                val result = RetrofitClient.apiService.searchBooks(query)
                SearchUiState.Success(result.items ?: emptyList())
            } catch (_: IOException) {
                SearchUiState.Error
            } catch (_: Exception) {
                SearchUiState.Error
            }
        }
    }
}
