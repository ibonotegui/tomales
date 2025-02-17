package io.github.ibonotegui.tomales

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import io.github.ibonotegui.tomales.repository.LocalDatasource
import io.github.ibonotegui.tomales.repository.Repository
import io.github.ibonotegui.tomales.ui.theme.TomalesTheme
import io.github.ibonotegui.tomales.view.AddItemAlertDialog
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import io.github.ibonotegui.tomales.viewmodel.UIState
import io.github.ibonotegui.tomales.viewmodel.ViewModelFactory
import kotlinx.coroutines.delay

const val TAG = "tomales"

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        //val repository = Repository(NetworkDatasource())
        // using a local data source simplifies UI testing
        val repository = Repository(LocalDatasource())
        // our viewmodel will be reused when orientation changes and onCreate is called
        val mainViewModel = ViewModelProvider(viewModelStore, ViewModelFactory(repository))[MainViewModel::class.java]

        setContent {
            TomalesTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        colors = topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary,
                        ),
                        title = {
                            Text(stringResource(R.string.app_name))
                        }
                    )
                }, floatingActionButton = {
                    var isDialogVisible by remember { mutableStateOf(false) }
                    FloatingActionButton(
                        onClick = {
                            isDialogVisible = true
                        },
                    ) {
                        Icon(Icons.Filled.Add, stringResource(R.string.add_item))
                    }
                    if (isDialogVisible) {
                        var listId by remember { mutableStateOf("") }
                        AddItemAlertDialog(
                            title = {
                                Text(text = stringResource(R.string.new_item))
                            },
                            content = {
                                OutlinedTextField(
                                    value = listId,
                                    onValueChange = {
                                        listId = it
                                    },
                                    label = { Text(stringResource(R.string.list_id)) },
                                )
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { isDialogVisible = false },
                                    content = { Text(stringResource(R.string.cancel)) },
                                )
                            },
                            confirmButton = {
                                val context = LocalContext.current
                                val text = stringResource(R.string.invalid_list_id)
                                TextButton(
                                    onClick = {
                                        if (listId.toIntOrNull() != null) {
                                            mainViewModel.addItem(listId = listId.toInt())
                                            isDialogVisible = false
                                        } else {
                                            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    content = { Text(stringResource(R.string.add_item)) },
                                )
                            },
                            onDismiss = {
                                isDialogVisible = false
                            },
                        )
                    }
                }, modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ItemsList(mainViewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemsList(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val uiState by remember { mainViewModel.uiStateFlow }.collectAsState()
    Log.d(TAG, "uiState $uiState")
    when (uiState) {
        UIState.IDLE -> {
            mainViewModel.getSortedItems()
        }

        UIState.LOADING -> {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = modifier.width(48.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        UIState.SUCCESS -> {
            val itemsMap = mainViewModel.itemsMap
            if (itemsMap.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_data_message),
                    fontSize = 18.sp,
                    modifier = modifier
                        .padding(10.dp)
                        .fillMaxSize()
                )
            } else {
                LazyColumn(modifier = modifier.fillMaxSize()) {
                    itemsMap.forEach { category ->
                        stickyHeader {
                            CategoryHeader(
                                category = category.key.toString(), modifier = Modifier.fillMaxWidth()
                            )
                        }
                        items(items = category.value) { item ->
                            SwipeToDeleteContainer(
                                item = item,
                                onDelete = {
                                    mainViewModel.deleteItem(item.id, item.listId)
                                    Log.d(TAG, "deleted item id " + item.id)
                            }) {
                                Text(
                                    text = "${item.name}",
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        else -> {
            Column(modifier = modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.error_message),
                    fontSize = 18.sp
                )
                Button(
                    onClick = {
                        mainViewModel.getSortedItems()
                    }
                ) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeToDeleteContainer(
    item: T,
    onDelete: (T) -> Unit,
    animationDuration: Int = 600,
    content: @Composable (T) -> Unit
) {
    var isDeleted by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    isDeleted = true
                }
                SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
            }
            return@rememberSwipeToDismissBoxState true
        },
        positionalThreshold = { it * .25f }
    )

    LaunchedEffect(key1 = isDeleted) {
        if (isDeleted) {
            delay(animationDuration.toLong())
            onDelete(item)
        }
    }

    AnimatedVisibility(
        visible = !isDeleted,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                DeleteItemRow(swipeToDismissBoxState = dismissState, onDelete = {
                    isDeleted = true
                })
            },
            enableDismissFromEndToStart = true,
            enableDismissFromStartToEnd = false,
            content = {
                content(item)
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteItemRow(swipeToDismissBoxState: SwipeToDismissBoxState, onDelete: () -> Unit) {
    val color = when (swipeToDismissBoxState.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Color.Green
        SwipeToDismissBoxValue.EndToStart -> Color.Red
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(12.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Icon(
            Icons.Default.Delete, //Icons.Outlined.Delete
            contentDescription = "delete item",
            tint = Color.White,
            modifier = Modifier.clickable {
                onDelete()
            }
        )
    }
}
