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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class SearchViewModel : ViewModel() {
    private val _searchUiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchUiState: StateFlow<SearchUiState> = _searchUiState.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    

    private var lastSearchedQuery: String? = null

    init {
        observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->

                if (query.length < 3 || query == lastSearchedQuery) {
                    _suggestions.value = emptyList()
                    return@onEach
                }


                if (_searchUiState.value !is SearchUiState.Loading) {
                    fetchSuggestions(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _searchQuery.value = query

        if (query.isBlank()) {
           clearSuggestions()
            lastSearchedQuery = null
        }
    }

    private suspend fun fetchSuggestions(query: String) {
        try {
            val result = RetrofitClient.apiService.searchBooks(query)
            val titles = result.items?.map { it.volumeInfo.title.trim() }
                ?.filter { it.isNotBlank() }
                ?.distinctBy { it.lowercase() }
                ?.take(5) ?: emptyList()
            

            if (_searchQuery.value == query && query != lastSearchedQuery) {
                _suggestions.value = titles
            }
        } catch (_: Exception) {
            _suggestions.value = emptyList()
        }
    }

    fun search(query: String, genre: String?) {
        val finalQuery = buildString {
            if (query.isNotBlank()) append(query)
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
        
        viewModelScope.launch {
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
