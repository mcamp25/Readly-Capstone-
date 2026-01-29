package com.example.mcamp25.readstack

import com.example.mcamp25.readstack.ui.screens.search.SearchUiState
import com.example.mcamp25.readstack.ui.screens.search.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {

        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {

        Dispatchers.resetMain()
    }

    @Test
    fun viewModel_initialState_isIdle() {
        val viewModel = SearchViewModel()
        // Verifies that when the app starts, it shows the welcome screen (Idle)
        assertTrue(viewModel.searchUiState.value is SearchUiState.Idle)
    }
}
