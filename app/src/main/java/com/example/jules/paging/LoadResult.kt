package com.example.jules.paging

sealed class LoadResult<Key : Any, Value : Any> {
    data class Page<Key : Any, Value : Any>(
        val data: List<Value>,
        val prevKey: Key?,
        val nextKey: Key?
    ) : LoadResult<Key, Value>()

    data class Error<Key : Any, Value : Any>(
        val throwable: Throwable
    ) : LoadResult<Key, Value>()
}
