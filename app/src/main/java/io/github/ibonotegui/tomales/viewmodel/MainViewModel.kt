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

enum class UIState {
    IDLE, LOADING, SUCCESS, ERROR
}

class MainViewModel(private val repository: Repository, private val dispatcher: CoroutineDispatcher) : ViewModel() {

    private var itemsList: MutableList<Item> = mutableStateListOf()

    private val _uiStateFlow = MutableStateFlow(UIState.IDLE)
    val uiStateFlow: StateFlow<UIState>
        get() = _uiStateFlow.asStateFlow()

    private val _mutableItemsMap = mutableMapOf<Int, List<Item>>()
    //if no UI update is required we could use a mutableStateMapOf
    //private val _mutableItemsMap = mutableStateMapOf<Int, List<Item>>()
    val itemsMap: Map<Int, List<Item>>
        get() = _mutableItemsMap

    fun getSortedItems() = viewModelScope.launch(dispatcher) {
            try {
                _uiStateFlow.emit(UIState.LOADING)
                itemsList = repository.getItemList().toMutableList()
                if (itemsList.isNotEmpty()) {
                    _mutableItemsMap.putAll(sortItems(itemsList))
                }
                _uiStateFlow.emit(UIState.SUCCESS)
            } catch (exception: Exception) {
                _uiStateFlow.emit(UIState.ERROR)
            }
        }

    fun addItem(listId: Int = Random.nextInt(4)) = viewModelScope.launch(dispatcher) {
        _uiStateFlow.emit(UIState.LOADING)
        delay(500)
        val newId = itemsList.size + 1
        val newItem = Item(id = newId, listId = listId, name = "Item $newId")
        itemsList.add(newItem)
        _mutableItemsMap.clear()
        _mutableItemsMap.putAll(sortItems(itemsList))
        _uiStateFlow.emit(UIState.SUCCESS)
    }

    fun deleteItem(itemId: Int, listId: Int) = viewModelScope.launch(dispatcher) {
        itemsList.removeIf { it.id == itemId }
        _mutableItemsMap.clear()
        _mutableItemsMap.putAll(sortItems(itemsList))
        if (!_mutableItemsMap.containsKey(listId)) {
            _uiStateFlow.emit(UIState.LOADING)
            delay(500)
            _uiStateFlow.emit(UIState.SUCCESS)
        }
    }

    private fun sortItems(itemList: List<Item>): Map<Int, List<Item>> {
        // since the last part of 'name' matches 'id' I decided to sort
        // by 'id' instead of converting the name substring to an Int and then sort it
        return itemList.filter {
            !it.name.isNullOrEmpty()
        }.sortedWith(compareBy<Item> { it.listId }.thenBy { it.id }).groupBy { it.listId }.toMap()
    }

}
