package io.github.ibonotegui.tomales.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.ibonotegui.tomales.model.Item
import io.github.ibonotegui.tomales.repository.Repository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class UIState {
    IDLE, LOADING, SUCCESS, ERROR
}

class MainViewModel(private val repository: Repository, private val dispatcher: CoroutineDispatcher) : ViewModel() {

    private val _uiStateFlow = MutableStateFlow(UIState.IDLE)
    val uiStateFlow: StateFlow<UIState>
        get() = _uiStateFlow.asStateFlow()

    private val _mutableItemsMap = mutableMapOf<Int, List<Item>>()
    val itemsMap: Map<Int, List<Item>>
        get() = _mutableItemsMap

    fun getSortedItems() = viewModelScope.launch(dispatcher) {
            try {
                _uiStateFlow.emit(UIState.LOADING)
                val itemList = repository.getItemList()
                if (itemList.isNotEmpty()) {
                    // since the last part of 'name' matches 'id' I decided to sort
                    // by 'id' instead of converting the name substring to an Int and then sort it
                    val filteredList = itemList.filter {
                        !it.name.isNullOrEmpty()
                    }.sortedWith(compareBy<Item> { it.listId }.thenBy { it.id }).groupBy { it.listId }.toMap()
                    _mutableItemsMap.putAll(filteredList)
                }
                _uiStateFlow.emit(UIState.SUCCESS)
            } catch (exception: Exception) {
                _uiStateFlow.emit(UIState.ERROR)
            }
        }
}
