package io.github.ibonotegui.tomales

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import io.github.ibonotegui.tomales.view.SwipeToDeleteContainer
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import io.github.ibonotegui.tomales.viewmodel.UIState
import io.github.ibonotegui.tomales.viewmodel.ViewModelFactory

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
                    ItemsLazyList(mainViewModel, modifier = Modifier.padding(innerPadding))
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
fun ItemsLazyList(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val uiState by remember { mainViewModel.uiStateFlow }.collectAsState()
    Log.d(TAG, "uiState $uiState")
    when (uiState) {
        is UIState.Idle -> {
            mainViewModel.getSortedItems()
        }

        is UIState.Loading -> {
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
                                    mainViewModel.deleteItem(item.item.id, item.item.listId)
                                    Log.d(TAG, "deleted item id " + item.item.id)
                            }) {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.item.name}",
                                        fontSize = 18.sp
                                    )
                                    val isCheckedState by remember { item.isFavorite }
                                    Checkbox(
                                        checked = isCheckedState,
                                        onCheckedChange = { isChecked ->
                                            mainViewModel.setIsFavorite(item.item.id, isFavorite = isChecked)
                                        },
                                    )
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
