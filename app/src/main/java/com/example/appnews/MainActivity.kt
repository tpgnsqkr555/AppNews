package com.example.appnews

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appnews.ui.theme.AppNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    companion object {
        const val API_KEY = "9057dba32aec4d66afcafe8c2f69e630"
        const val SPORT = "NBA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNewsTheme {
                SportNewsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportNewsScreen() {
    val context = LocalContext.current
    var article by remember { mutableStateOf<Article?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cacheManager = remember { CacheManager(context) }
    val cronetClient = remember { CronetClient(context) }

    LaunchedEffect(true) {
        try {
            if (!cacheManager.isCacheExpired()) {
                Toast.makeText(context, "Loading from cache...", Toast.LENGTH_SHORT).show()
                val cachedData = cacheManager.loadCachedArticle()
                if (cachedData != null) {
                    article = cachedData.first
                    bitmap = cachedData.second
                    isLoading = false
                    return@LaunchedEffect
                }
            }

            Toast.makeText(context, "Fetching new data...", Toast.LENGTH_SHORT).show()
            val response = cronetClient.fetchSportsNews(
                sport = MainActivity.SPORT,
                apiKey = MainActivity.API_KEY
            )

            val articlesWithImages = response.articles.filter { !it.imageUrl.isNullOrEmpty() }
            if (articlesWithImages.isNotEmpty()) {
                val selectedArticle = articlesWithImages.random()
                article = selectedArticle

                // Download and cache image
                selectedArticle.imageUrl?.let { imageUrl ->
                    bitmap = cacheManager.downloadAndCacheImage(imageUrl)
                }

                // Cache the article and image
                cacheManager.cacheArticle(selectedArticle, bitmap)
                Toast.makeText(context, "New article cached", Toast.LENGTH_SHORT).show()
            } else {
                error = "No articles with images found"
            }
        } catch (e: Exception) {
            error = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("${MainActivity.SPORT} News") },
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
                    Text(
                        text = error ?: "Unknown error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    ArticleContent(
                        article = article,
                        bitmap = bitmap
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleContent(
    article: Article?,
    bitmap: Bitmap?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Article image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        article?.title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        article?.source?.name?.let {
            Text(
                text = "Source: $it",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        article?.content?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}