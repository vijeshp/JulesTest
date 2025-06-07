package com.example.jules.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.jules.network.ListItemModel
import com.example.jules.paging.PagingData

// This would typically be provided by a DI framework or passed as a parameter.
// For simplicity here, we're assuming a way to get the ViewModel instance.
// In a real app, you might use viewModel<MyViewModel>() if you set up ViewModelFactory.
// For now, this Composable will need the ViewModel passed to it.

@Composable
fun MyListScreen(viewModel: MyViewModel) {
    // Collect the flow of PagingData.
    // The initial value PagingData(emptyList()) is important as pager.flow might not emit immediately.
    val pagingDataState by viewModel.itemsFlow.collectAsState(initial = PagingData(emptyList()))

    // TODO: Observe loading and error states from ViewModel/Pager (when implemented in Step 7)
    // val isLoading by viewModel.isLoading.collectAsState() // Conceptual
    // val error by viewModel.error.collectAsState() // Conceptual

    Column(modifier = Modifier.fillMaxSize()) {
        // TODO: Loading State UI (Conceptual)
        // if (isLoading && pagingDataState.items.isEmpty()) {
        //     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        //         CircularProgressIndicator()
        //     }
        //     return@Column
        // }

        // TODO: Error State UI (Conceptual)
        // if (error != null && pagingDataState.items.isEmpty()) {
        //     Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        //         Text("Error: ${error.message}", color = MaterialTheme.colorScheme.error)
        //         Button(onClick = { viewModel.refreshList() }) {
        //             Text("Retry")
        //         }
        //     }
        //     return@Column
        // }

        if (pagingDataState.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items to display. Pull to refresh or check connection.")
                // Potentially add a refresh button here too if not using pull-to-refresh
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(
                    items = pagingDataState.items,
                    key = { item -> item.id } // Assuming ListItemModel has a stable 'id'
                ) { item ->
                    ListItemRow(
                        item = item,
                        onUpdate = { viewModel.updateItem(item) }, // Or pass item to an update screen
                        onDelete = { viewModel.deleteItem(item.id) }
                    )
                    Divider()
                }

                // TODO: Append Loading State UI (Conceptual)
                // if (isLoading && pagingDataState.items.isNotEmpty()) {
                //     item {
                //         Row(
                //             modifier = Modifier.fillMaxWidth().padding(8.dp),
                //             horizontalArrangement = Arrangement.Center
                //         ) {
                //             CircularProgressIndicator()
                //         }
                //     }
                // }
            }
        }

        Button(onClick = { viewModel.refreshList() }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp)) {
            Text("Refresh List (Conceptual)")
        }
    }
}

@Composable
fun ListItemRow(
    item: ListItemModel,
    onUpdate: (ListItemModel) -> Unit,
    onDelete: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(text = item.description, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onUpdate(item) }) {
            Text("Update")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onDelete(item.id) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Delete")
        }
    }
}

// TODO: Need to integrate this screen into an Activity, typically MainActivity.kt,
// and provide the MyViewModel instance. For now, the file is self-contained.
// Example of how it might be called in MainActivity's setContent block:
//
// val apiService = ApiService.create() // Or from DI
// val viewModelFactory = MyViewModelFactory(apiService)
// val myViewModel: MyViewModel by viewModels { viewModelFactory }
// MyListScreen(viewModel = myViewModel)
//
// Also need a ViewModelFactory if MyViewModel has constructor params.

// Create MyViewModelFactory.kt in the same package for ViewModel instantiation
