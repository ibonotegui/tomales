package io.github.ibonotegui.tomales.view

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ibonotegui.tomales.R
import io.github.ibonotegui.tomales.TAG
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import io.github.ibonotegui.tomales.viewmodel.UIState

@Composable
fun CategoryHeader(category: String, modifier: Modifier = Modifier) {
    Text(
        text = "ListId $category",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(10.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemsLazyList(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val uiState by remember { mainViewModel.uiStateFlow }.collectAsState()
    Log.d(TAG, "uiState $uiState")
    when (uiState) {
        is UIState.Idle -> {
            mainViewModel.getSortedItems()
        }

        is UIState.Loading -> {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = modifier.width(48.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        is UIState.Success -> {
            val itemsMap = (uiState as UIState.Success).items
            if (itemsMap.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_data_message),
                    fontSize = 18.sp,
                    modifier = modifier
                        .padding(10.dp)
                        .fillMaxSize()
                )
            } else {
                val state = rememberPullToRefreshState()
                val isRefreshing = remember { mutableStateOf(false) }
                PullToRefreshBox(
                    isRefreshing = isRefreshing.value, onRefresh = {
                        mainViewModel.getSortedItems()
                    }, state = state
                ) {
                    LazyColumn(modifier = modifier.fillMaxSize()) {
                        itemsMap.forEach { category ->
                            stickyHeader {
                                CategoryHeader(
                                    category = category.key.toString(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            items(items = category.value, key = { it.item.id }) { item ->
                                SwipeToDeleteContainer(item = item, onDelete = {
                                    mainViewModel.deleteItem(item.item.id, item.item.listId)
                                }) {
                                    Row(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${item.item.name}", fontSize = 18.sp
                                        )
                                        val isCheckedState by remember { item.isFavorite }
                                        Checkbox(
                                            checked = isCheckedState,
                                            onCheckedChange = { isChecked ->
                                                mainViewModel.setIsFavorite(
                                                    item.item.id, isFavorite = isChecked
                                                )
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        is UIState.Error -> {
            val errorMessage = (uiState as UIState.Error).message
            Column(modifier = modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.error_message) + "\n" + errorMessage,
                    fontSize = 18.sp
                )
                Button(onClick = {
                    mainViewModel.getSortedItems()
                }) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}
