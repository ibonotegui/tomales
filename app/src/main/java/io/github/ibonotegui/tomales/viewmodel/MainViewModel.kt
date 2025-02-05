package io.github.ibonotegui.tomales.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.ibonotegui.tomales.model.Item
import io.github.ibonotegui.tomales.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.SortedMap

enum class Status {
    LOADING, SUCCESS, ERROR
}

class MainViewModel(private val repository: Repository) : ViewModel() {

    private val _mutableItemsMap = MutableLiveData<SortedMap<Int, List<Item>>>()
    val itemsLiveData: LiveData<SortedMap<Int, List<Item>>>
        get() = _mutableItemsMap

    private val _uiState = MutableLiveData<Status>()
    val uiState: LiveData<Status>
        get() = _uiState

    fun getSortedItemsMap() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.postValue(Status.LOADING)
                val itemList = repository.getItemList()
                if (!itemList.isNullOrEmpty()) {
                    // since the last part of 'name' matches 'id' I decided to sort
                    // by 'id' instead of converting the name substring to an Int and then sort it
                    val filteredList = itemList.filter {
                        !it.name.isNullOrEmpty()
                    }.sortedWith(compareBy<Item> { it.listId }.thenBy { it.id }).groupBy { it.listId }.toSortedMap()
                    _mutableItemsMap.postValue(filteredList)
                }
                _uiState.postValue(Status.SUCCESS)
            } catch (exception: Exception) {
                _uiState.postValue(Status.ERROR)
            }
        }
    }

}
