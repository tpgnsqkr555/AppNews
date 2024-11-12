package com.example.appnews

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appnews.ui.theme.AppNewsTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    companion object {
        const val API_KEY = "9057dba32aec4d66afcafe8c2f69e630"
        const val BASE_URL = "https://newsapi.org/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNewsTheme {
                NewsApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp() {
    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // fetch news when the app starts
    LaunchedEffect(true) {
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(MainActivity.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val newsApi = retrofit.create(NewsApi::class.java)

            Log.d("NewsApp", "Starting API request")
            val response = newsApi.getAndroidNews(apiKey = MainActivity.API_KEY)
            Log.d("NewsApp", "Got response: ${response.articles.size} articles")

            articles = response.articles
        } catch (e: Exception) {
            Log.e("NewsApp", "Error fetching news: ${e.message}", e)
            error = "API Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Android News") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = error ?: "Unknown error occurred",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    NewsList(articles = articles)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun NewsAppPreview() {
    AppNewsTheme {
        NewsApp()
    }
}