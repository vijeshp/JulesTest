package com.example.jules.paging

import com.example.jules.network.ApiService
import com.example.jules.network.ListItemModel
import retrofit2.HttpException
import java.io.IOException

class MyPagingSource(
    private val apiService: ApiService
) : CustomPagingSource<Int, ListItemModel>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListItemModel> {
        val pageNumber = params.key ?: 1 // Default to page 1 if no key is provided
        val pageSize = params.loadSize

        return try {
            val response = apiService.getItems(page = pageNumber, limit = pageSize)
            val items = response.items
            val nextKey = response.nextPage
            // Assuming API page numbers are 1-indexed. If 0-indexed, prevKey logic might change.
            val prevKey = if (pageNumber == 1) null else pageNumber - 1

            LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: IOException) {
            // IOException for network failures
            LoadResult.Error(e)
        } catch (e: HttpException) {
            // HttpException for non-2xx HTTP status codes
            LoadResult.Error(e)
        }
    }
}
