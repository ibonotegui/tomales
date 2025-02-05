package io.github.ibonotegui.tomales

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ibonotegui.tomales.repository.NetworkDatasource
import io.github.ibonotegui.tomales.repository.Repository
import io.github.ibonotegui.tomales.ui.theme.TomalesTheme
import io.github.ibonotegui.tomales.viewmodel.MainViewModel
import io.github.ibonotegui.tomales.viewmodel.Status

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mainViewModel = MainViewModel(Repository(NetworkDatasource()))
        // using a local data source simplifies UI testing
        // val mainViewModel = MainViewModel(Repository(LocalDatasource()))
        mainViewModel.getSortedItemsMap()

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
                    List(mainViewModel, modifier = Modifier.padding(innerPadding))
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
fun List(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val status = mainViewModel.uiState.observeAsState()
    when (status.value) {
        Status.LOADING -> {
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
        Status.SUCCESS -> {
            val itemsSortedMap = mainViewModel.itemsLiveData.value
            LazyColumn(modifier = modifier.fillMaxSize()) {
                itemsSortedMap?.forEach { cat ->
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
        else -> {
            Text(
                text = stringResource(R.string.error_message),
                fontSize = 18.sp,
                modifier = modifier.padding(10.dp).fillMaxSize()
            )
        }
    }
}
