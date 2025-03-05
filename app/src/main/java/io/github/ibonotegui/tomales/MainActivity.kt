package io.github.ibonotegui.tomales

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.github.ibonotegui.tomales.ui.theme.TomalesTheme
import io.github.ibonotegui.tomales.view.AddItemAlertDialog
import io.github.ibonotegui.tomales.view.ItemsLazyList
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.androidx.compose.koinViewModel

const val TAG = "tomales"

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TomalesTheme {
                val mainViewModel: MainViewModel = koinViewModel(viewModelStoreOwner = LocalActivity.current as MainActivity)
                KoinAndroidContext {
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
}
