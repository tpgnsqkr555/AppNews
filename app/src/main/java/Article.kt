package com.example.appnews

import com.google.gson.annotations.SerializedName

data class Article(
    val title: String = "",
    val author: String? = null,
    val content: String? = null,
    @SerializedName("urlToImage")
    val imageUrl: String? = null,
    val publishedAt: String = "",
    val source: Source = Source("")
)

data class Source(
    val name: String = ""
)

data class NewsResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val articles: List<Article> = emptyList()
)

data class CacheMetadata(
    val timestamp: Long,
    val imageFilePath: String,
    val contentFilePath: String
)