package com.example.jules.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jules.network.ApiService
import com.example.jules.network.ListItemModel
import com.example.jules.paging.*
import kotlinx.coroutines.flow.* // Imports StateFlow, MutableStateFlow, combine, stateIn, SharingStarted
import kotlinx.coroutines.launch
import retrofit2.Response

class MyViewModel(private val apiService: ApiService) : ViewModel() {

    private val pagingConfig = PagingConfig(
        pageSize = 20,
        prefetchDistance = 5, // Not actively used by CustomPager logic yet
        initialLoadSize = 20
    )

    private val pagingSourceFactory = { MyPagingSource(apiService) }

    private val pager = CustomPager<Int, ListItemModel>(
        config = pagingConfig,
        initialKey = 1,
        pagingSourceFactory = pagingSourceFactory
    )

    val itemsFlow: StateFlow<PagingData<ListItemModel>> = pager.pagingDataFlow

    // For ViewModel-specific actions that need to temporarily override or set specific load states
    private val _actionLoadStateOverride = MutableStateFlow<LoadState?>(null)

    // Combine pager's load state with any override from ViewModel actions
    val loadStateFlow: StateFlow<LoadState> = combine(
        pager.loadStateFlow,
        _actionLoadStateOverride
    ) { pagerState, overrideState ->
        overrideState ?: pagerState // If override is set, use it, otherwise use pager's state
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = pager.loadStateFlow.value // Initial value from pager's current state
    )

    fun refreshList() {
        _actionLoadStateOverride.value = null // Clear any action-specific override
        pager.refresh()
    }

    fun loadNextItems() {
        // Only request next page if not already loading (overall state)
        if (loadStateFlow.value !is LoadState.LoadingInitial && loadStateFlow.value !is LoadState.LoadingAppend) {
             _actionLoadStateOverride.value = null // Clear any action-specific override
             pager.requestLoadNextPage()
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            _actionLoadStateOverride.value = LoadState.LoadingInitial // Show loading for this action
            try {
                val response: Response<Unit> = apiService.deleteListItem(itemId)
                if (response.isSuccessful) {
                    _actionLoadStateOverride.value = null // Clear override, refresh will set its own states
                    pager.refresh()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error deleting item"
                    _actionLoadStateOverride.value = LoadState.Error(RuntimeException("Delete failed: $errorMessage (item $itemId)"))
                }
            } catch (e: Exception) {
                 _actionLoadStateOverride.value = LoadState.Error(e)
            }
        }
    }

    fun updateItem(item: ListItemModel) {
        viewModelScope.launch {
            _actionLoadStateOverride.value = LoadState.LoadingInitial // Show loading for this action
            try {
                val response: Response<ListItemModel> = apiService.updateListItem(item.id, item)
                if (response.isSuccessful) {
                    _actionLoadStateOverride.value = null // Clear override, refresh will set its own states
                    pager.refresh()
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error updating item"
                    _actionLoadStateOverride.value = LoadState.Error(RuntimeException("Update failed: $errorMessage (item ${item.id})"))
                }
            } catch (e: Exception) {
                _actionLoadStateOverride.value = LoadState.Error(e)
            }
        }
    }
}
