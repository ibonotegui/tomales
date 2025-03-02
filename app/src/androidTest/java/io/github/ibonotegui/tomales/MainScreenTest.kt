package io.github.ibonotegui.tomales

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.ibonotegui.tomales.repository.LocalDatasource
import io.github.ibonotegui.tomales.repository.Repository
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import org.junit.Rule
import org.junit.Test

class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainScreen_dataDisplayed() {
        val repository = Repository(LocalDatasource())
        val mainViewModel = MainViewModel(repository)
        mainViewModel.getSortedItems()

        composeTestRule.setContent {
            ItemsLazyList(mainViewModel)
        }

        composeTestRule
            .onNodeWithText("Item 4")
            .assertIsDisplayed()
    }
}
