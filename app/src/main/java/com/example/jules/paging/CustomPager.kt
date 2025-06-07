package com.example.jules.paging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.CopyOnWriteArrayList

class CustomPager<Key : Any, Value : Any>(
    private val config: PagingConfig,
    private var initialKey: Key?, // Var because refresh resets it
    private val pagingSourceFactory: () -> CustomPagingSource<Key, Value>
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentPagingSource: CustomPagingSource<Key, Value> = pagingSourceFactory()
    private val loadedItems = CopyOnWriteArrayList<Value>()
    private var currentPageKey: Key? = initialKey // Key for the NEXT page to load
    private val loadMutex = Mutex()

    private val _pagingDataFlow = MutableStateFlow(PagingData<Value>(emptyList()))
    val pagingDataFlow: StateFlow<PagingData<Value>> = _pagingDataFlow.asStateFlow()

    private val _loadStateFlow = MutableStateFlow<LoadState>(LoadState.NotLoading)
    val loadStateFlow: StateFlow<LoadState> = _loadStateFlow.asStateFlow()

    init {
        // Trigger initial load
        requestLoadNextPage(isInitialLoad = true)
    }

    fun requestLoadNextPage(isInitialLoad: Boolean = false) {
        scope.launch {
            val keyForThisLoad = if (isInitialLoad) initialKey else currentPageKey

            // If not an initial load and no next key, we're at the end.
            if (keyForThisLoad == null && !isInitialLoad) {
                _loadStateFlow.value = LoadState.NotLoading
                return@launch
            }

            if (loadMutex.tryLock()) {
                try {
                    _loadStateFlow.value = if (isInitialLoad || loadedItems.isEmpty()) LoadState.LoadingInitial else LoadState.LoadingAppend

                    val loadSize = if (isInitialLoad || loadedItems.isEmpty()) config.initialLoadSize else config.pageSize

                    when (val result = currentPagingSource.load(LoadParams(key = keyForThisLoad, loadSize = loadSize))) {
                        is LoadResult.Page -> {
                            if (isInitialLoad) { // If it's an initial load (refresh or first time)
                                loadedItems.clear()
                            }
                            loadedItems.addAll(result.data)
                            _pagingDataFlow.value = PagingData(ArrayList(loadedItems)) // Emit copy
                            currentPageKey = result.nextKey
                            _loadStateFlow.value = LoadState.NotLoading
                        }
                        is LoadResult.Error -> {
                            _loadStateFlow.value = LoadState.Error(result.throwable)
                        }
                    }
                } finally {
                    loadMutex.unlock()
                }
            }
            // If tryLock fails, another load is already in progress, so do nothing.
        }
    }

    fun refresh() {
        scope.launch {
            loadMutex.lock() // Ensure exclusive access for resetting state
            try {
                currentPagingSource = pagingSourceFactory() // Get a fresh PagingSource
                currentPageKey = initialKey // Reset to initial key
            } finally {
                loadMutex.unlock()
            }
            // Trigger a new initial load, which will clear items and set loading states
            requestLoadNextPage(isInitialLoad = true)
        }
    }
}
