package com.example.jules.paging

// Initial simple version. May need to be enhanced later for updates/deletes.
data class PagingData<Value : Any>(
    val items: List<Value>
)
