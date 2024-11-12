package com.example.appnews

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v2/everything")
    suspend fun getAndroidNews(
        @Query("q") query: String = "android",
        @Query("apiKey") apiKey: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt"
    ): NewsResponse
}