package com.example.mcamp25.readstack

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mcamp25.readstack.ui.screens.search.SearchScreen
import com.example.mcamp25.readstack.ui.screens.search.SearchViewModel
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchScreen_initialState_showsWelcomeText() {
        composeTestRule.setContent {
            SearchScreen(
                viewModel = SearchViewModel(),
                onBookClick = {}
            )
        }

        // Checks if the updated welcome text is visible
        composeTestRule
            .onNodeWithText("Find your next great read.")
            .assertIsDisplayed()
    }
}
