package io.github.ibonotegui.tomales.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.ibonotegui.tomales.model.Item
import io.github.ibonotegui.tomales.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class UIState {
    data object Idle : UIState()
    data object Loading : UIState()
    data class Success(val items: Map<Int, List<Item>>) : UIState()
    data class Error(val message: String?) : UIState()
}

class MainViewModel(private val repository: Repository, private val dispatcher: CoroutineDispatcher) : ViewModel() {

    private var itemsList: MutableList<Item> = mutableStateListOf()

    private val _uiStateFlow = MutableStateFlow<UIState>(UIState.Idle)
    val uiStateFlow: StateFlow<UIState>
        get() = _uiStateFlow.asStateFlow()

    fun getSortedItems() = viewModelScope.launch(dispatcher) {
            try {
                _uiStateFlow.emit(UIState.Loading)
                itemsList = repository.getItemList().toMutableList()
                _uiStateFlow.emit(UIState.Success(sortItems(itemsList)))
            } catch (exception: Exception) {
                _uiStateFlow.emit(UIState.Error(exception.message))
            }
        }

    fun addItem(listId: Int = Random.nextInt(4)) = viewModelScope.launch(dispatcher) {
        _uiStateFlow.emit(UIState.Loading)
        delay(500)
        val newId = itemsList.size + 1
        val newItem = Item(id = newId, listId = listId, name = "Item $newId")
        itemsList.add(newItem)
        _uiStateFlow.emit(UIState.Success(sortItems(itemsList)))
    }

    fun deleteItem(itemId: Int, listId: Int) = viewModelScope.launch(dispatcher) {
        itemsList.removeIf { it.id == itemId }
        if (itemsList.find { it.listId == listId } == null) {
            _uiStateFlow.emit(UIState.Loading)
            delay(500)
            _uiStateFlow.emit(UIState.Success(sortItems(itemsList)))
        }
    }

    private fun sortItems(itemList: List<Item>): Map<Int, List<Item>> {
        // since the last part of 'name' matches 'id' I decided to sort
        // by 'id' instead of converting the name substring to an Int and then sort it
        return itemList.filter {
            !it.name.isNullOrEmpty()
        }.sortedWith(compareBy<Item> { it.listId }.thenBy { it.id }).groupBy { it.listId }.toMap()
    }

    fun setIsFavorite(itemId: Int, isFavorite: Boolean) {
        itemsList.forEach { item ->
            if (item.id == itemId) {
                item.isFavorite = isFavorite
            }
        }
    }
}
