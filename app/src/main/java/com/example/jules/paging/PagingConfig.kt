package com.example.jules.paging

data class PagingConfig(
    val pageSize: Int,
    val prefetchDistance: Int = pageSize, // Default prefetchDistance to pageSize
    val initialLoadSize: Int = pageSize * 3 // Default initial load size
)
