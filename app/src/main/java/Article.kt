package com.example.appnews

// Data class for the news article
data class Article(
    val title: String = "",
    val publishedAt: String = "",
    val source: Source = Source("")
)

// Data class for the news source
data class Source(
    val name: String = ""
)

// Data class for the API response
data class NewsResponse(
    val status: String = "",
    val totalResults: Int = 0,
    val articles: List<Article> = emptyList()
)