package com.example.jules.paging

data class LoadParams<Key : Any>(
    val key: Key?,
    val loadSize: Int
)
