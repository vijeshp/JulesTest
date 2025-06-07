package com.example.jules.network

import retrofit2.Response // Import Response for delete/update methods
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body // Import for request body
import retrofit2.http.DELETE // Import for DELETE method
import retrofit2.http.GET
import retrofit2.http.PUT // Import for PUT method
import retrofit2.http.Path // Import for path parameters
import retrofit2.http.Query

interface ApiService {
    @GET("items") // Example endpoint for getting items
    suspend fun getItems(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): PaginatedResponse<ListItemModel>

    @DELETE("items/{id}") // Example endpoint for deleting an item
    suspend fun deleteListItem(@Path("id") id: String): Response<Unit> // Response<Unit> for empty response body

    @PUT("items/{id}") // Example endpoint for updating an item
    suspend fun updateListItem(
        @Path("id") id: String,
        @Body item: ListItemModel
    ): Response<ListItemModel> // Assuming API returns the updated item

    companion object {
        private const val BASE_URL = "https://api.example.com/" // Replace with your actual base URL

        fun create(): ApiService {
            // In a real app, you'd likely use a dependency injection framework.
            // Also, add OkHttpClient for logging interceptor or other customizations.
            // val logging = HttpLoggingInterceptor()
            // logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            // val client = OkHttpClient.Builder()
            //    .addInterceptor(logging)
            //    .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                // .client(client) // Uncomment to add custom OkHttpClient
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
