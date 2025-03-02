package io.github.ibonotegui.tomales

import io.github.ibonotegui.tomales.repository.LocalDatasource
import io.github.ibonotegui.tomales.repository.Repository
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import io.github.ibonotegui.tomales.viewmodel.UIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun mainViewModel_UIStateChanges() = runTest {
        val mainViewModel = MainViewModel(Repository(LocalDatasource()), StandardTestDispatcher(testScheduler))
        var currentUiState = mainViewModel.uiStateFlow.value
        assertEquals(UIState.Idle, currentUiState)
        mainViewModel.getSortedItems()
        advanceUntilIdle()
        assertEquals(mainViewModel.itemsList.isNotEmpty(), true)
        currentUiState = mainViewModel.uiStateFlow.value as UIState.Success
        assertEquals(UIState.Success(currentUiState.items), currentUiState)
    }
}
