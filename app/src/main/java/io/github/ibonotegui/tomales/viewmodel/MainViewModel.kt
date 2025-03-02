package io.github.ibonotegui.tomales.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.ibonotegui.tomales.model.Item
import io.github.ibonotegui.tomales.model.ItemUI
import io.github.ibonotegui.tomales.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed class UIState {
    data object Idle : UIState()
    data object Loading : UIState()
    data class Success(val items: Map<Int, List<ItemUI>>) : UIState()
    data class Error(val message: String?) : UIState()
}

class MainViewModel(private val repository: Repository, private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    var itemsList: MutableList<ItemUI> = mutableListOf()
        private set

    private val _uiStateFlow = MutableStateFlow<UIState>(UIState.Idle)
    val uiStateFlow: StateFlow<UIState>
        get() = _uiStateFlow.asStateFlow()

    fun getSortedItems() = viewModelScope.launch(dispatcher) {
            try {
                _uiStateFlow.emit(UIState.Loading)
                itemsList = repository.getItemList().map { item ->
                    val isFavorite = Random.nextInt(10) % 2 == 0
                    ItemUI(item, mutableStateOf(isFavorite))
                }.toMutableList()
                _uiStateFlow.emit(UIState.Success(sortItems(itemsList)))
            } catch (exception: Exception) {
                _uiStateFlow.emit(UIState.Error(exception.message))
            }
        }

    fun addItem(listId: Int = Random.nextInt(4)) = viewModelScope.launch(dispatcher) {
        _uiStateFlow.emit(UIState.Loading)
        delay(500)
        val newId = if (itemsList.size > 0) {
            itemsList.maxBy { it.item.id }.item.id.plus(1)
        } else {
            0
        }
        val newItem = Item(id = newId, listId = listId, name = "Item $newId")
        itemsList.add(ItemUI(newItem, mutableStateOf(false)))
        _uiStateFlow.emit(UIState.Success(sortItems(itemsList)))
    }

    fun deleteItem(itemId: Int, listId: Int) = viewModelScope.launch(dispatcher) {
        itemsList.removeIf { it.item.id == itemId }
        if (itemsList.find { it.item.listId == listId } == null) {
            _uiStateFlow.emit(UIState.Loading)
            delay(500)
            _uiStateFlow.emit(UIState.Success(sortItems(itemsList)))
        }
    }

    private fun sortItems(itemList: List<ItemUI>): Map<Int, List<ItemUI>> {
        return itemList.filter {
            !it.item.name.isNullOrEmpty()
        }.sortedWith(compareBy<ItemUI> { it.item.listId }.thenBy { it.item.id }).groupBy { it.item.listId }.toMap()
    }

    fun setIsFavorite(itemId: Int, isFavorite: Boolean) {
        itemsList.forEach { item ->
            if (item.item.id == itemId) {
                item.isFavorite.value = isFavorite
            }
        }
    }
}
