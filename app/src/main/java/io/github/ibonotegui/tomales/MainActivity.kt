package io.github.ibonotegui.tomales

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import io.github.ibonotegui.tomales.repository.NetworkDatasource
import io.github.ibonotegui.tomales.repository.Repository
import io.github.ibonotegui.tomales.ui.theme.TomalesTheme
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import io.github.ibonotegui.tomales.viewmodel.UIState
import io.github.ibonotegui.tomales.viewmodel.ViewModelFactory

const val TAG = "tomales"

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = Repository(NetworkDatasource())
        // using a local data source simplifies UI testing
        // val repository = Repository(LocalDatasource())
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
        text = "listId $category",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(10.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemsList(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val uiState by rememberUpdatedState(mainViewModel.uiStateFlow.collectAsState())
    Log.d(TAG, "uiState $uiState")
    when (uiState.value) {
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
                    modifier = modifier.padding(10.dp).fillMaxSize()
                )
            } else {
                LazyColumn(modifier = modifier.fillMaxSize()) {
                    itemsMap.forEach { cat ->
                        stickyHeader {
                            CategoryHeader(
                                category = cat.key.toString(), modifier = Modifier.fillMaxWidth()
                            )
                        }
                        items(cat.value) { item ->
                            Text(
                                text = "${item.name}",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(10.dp)
                            )
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
