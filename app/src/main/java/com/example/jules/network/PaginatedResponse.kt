package com.example.jules.network

data class PaginatedResponse<T>(
    val items: List<T>,
    val currentPage: Int,
    val nextPage: Int?,
    val totalItems: Int,
    val totalPages: Int
)
