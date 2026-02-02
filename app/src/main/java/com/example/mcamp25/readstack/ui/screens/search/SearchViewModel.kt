package com.example.mcamp25.readstack.ui.screens.search

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mcamp25.readstack.data.RetrofitClient
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class SearchViewModel : ViewModel() {
    private var searchJob: Job? = null
    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    

    private var lastSearchedQuery: String? = null

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSearchQuery() {
        _searchQuery
            .debounce(200)
            .distinctUntilChanged()
            .flatMapLatest { query ->
             flow {
                if (query.length >= 3 && query != lastSearchedQuery) {
                    emit(fetchSuggestions(query))
                } else {
                    emit(emptyList())
                }
            }
    }
    .onEach { titles -> _suggestions.value = titles  }
            .launchIn(viewModelScope)
}
    fun onQueryChanged(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
           clearSuggestions()
            lastSearchedQuery = null
        }
    }

    private suspend fun fetchSuggestions(query: String): List<String> {
        return try {
            val result = RetrofitClient.apiService.searchBooks(query)
            result.items?.map { it.volumeInfo.title.trim() }
                ?.filter { it.isNotBlank() }
                ?.distinctBy { it.lowercase() }
                ?.take(5) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }



    fun search(query: String, genre: String?) {
        val finalQuery = buildString {
            if (query.isNotBlank()) append("intitle:\"$query\"")
            if (genre != null) {
                if (isNotEmpty()) append(" ")
                val subject = if (genre == "Sci-Fi") "Science Fiction" else genre
                append("subject:\"$subject\"")
            }
        }

        lastSearchedQuery = query
        clearSuggestions()
        searchBooks(finalQuery)
    }

    private fun searchBooks(query: String) {
        if (query.isBlank()) return
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            try {
                val result = RetrofitClient.apiService.searchBooks(query)
                _searchUiState.value = SearchUiState.Success(result.items ?: emptyList())
            } catch (_: IOException) {
                _searchUiState.value = SearchUiState.Error
            } catch (_: Exception) {
                _searchUiState.value = SearchUiState.Error
            }
        }
    }

    fun searchByImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _searchUiState.value = SearchUiState.Loading
            try {
                val image = InputImage.fromFilePath(context, uri)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                
                recognizer.process(image)
                    .addOnSuccessListener { visionText -> 
                        val detectedText = visionText.text
                        if (detectedText.isNotBlank()) {
                            searchBooks(detectedText)
                        } else {
                            _searchUiState.value = SearchUiState.Error
                        }
                    }
                    .addOnFailureListener { 
                        _searchUiState.value = SearchUiState.Error
                    }
            } catch (_: Exception) {
                _searchUiState.value = SearchUiState.Error
            }
        }
    }

    fun resetSearch() {
        _searchUiState.value = SearchUiState.Idle
        clearSuggestions()
        lastSearchedQuery = null
    }

    fun handleImportedFile(uri: Uri) {
        println("Imported file: $uri")
    }
    
    fun clearSuggestions() {
        _suggestions.value = emptyList()
    }
}
