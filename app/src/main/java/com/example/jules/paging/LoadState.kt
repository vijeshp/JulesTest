package com.example.jules.paging

sealed class LoadState {
    object NotLoading : LoadState() // Covers idle and end of list
    object LoadingInitial : LoadState() // Specific state for initial load or refresh
    object LoadingAppend : LoadState() // Specific state for loading next page
    data class Error(val throwable: Throwable) : LoadState()
}
