package com.example.appnews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CacheManager(private val context: Context) {
    private val gson = Gson()
    private val cronetEngine = CronetEngine.Builder(context).build()
    private val executor = Executors.newSingleThreadExecutor()

    companion object {
        private const val CACHE_DURATION = 10_000L // 10 seconds
        private const val ARTICLE_FILE = "article.json"
        private const val IMAGE_FILE = "article_image.jpg"
        private const val TIMESTAMP_FILE = "cache_timestamp.txt"
    }

    fun isCacheExpired(): Boolean {
        val timestampFile = File(context.filesDir, TIMESTAMP_FILE)
        if (!timestampFile.exists()) return true

        val timestamp = timestampFile.readText().toLongOrNull() ?: return true
        return System.currentTimeMillis() - timestamp > CACHE_DURATION
    }

    fun cacheArticle(article: Article, bitmap: Bitmap?) {
        try {
            // Save article
            val articleFile = File(context.filesDir, ARTICLE_FILE)
            articleFile.writeText(gson.toJson(article))

            // Save bitmap
            bitmap?.let {
                val imageFile = File(context.filesDir, IMAGE_FILE)
                FileOutputStream(imageFile).use { fos ->
                    it.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                }
            }

            // Update timestamp
            val timestampFile = File(context.filesDir, TIMESTAMP_FILE)
            timestampFile.writeText(System.currentTimeMillis().toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadCachedArticle(): Pair<Article, Bitmap>? {
        try {
            val articleFile = File(context.filesDir, ARTICLE_FILE)
            val imageFile = File(context.filesDir, IMAGE_FILE)

            if (!articleFile.exists() || !imageFile.exists()) return null

            val article = gson.fromJson(articleFile.readText(), Article::class.java)
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)

            return if (article != null && bitmap != null) {
                Pair(article, bitmap)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun downloadAndCacheImage(url: String): Bitmap? = suspendCoroutine { continuation ->
        val requestBuilder = cronetEngine.newUrlRequestBuilder(
            url,
            object : UrlRequest.Callback() {
                private val receivedData = mutableListOf<Byte>()

                override fun onRedirectReceived(
                    request: UrlRequest?,
                    info: UrlResponseInfo?,
                    newLocationUrl: String?
                ) {
                    request?.followRedirect()
                }

                override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
                    if (info?.httpStatusCode == 200) {
                        request?.read(ByteBuffer.allocateDirect(32768))
                    } else {
                        continuation.resumeWithException(Exception("HTTP Error: ${info?.httpStatusCode}"))
                    }
                }

                override fun onReadCompleted(
                    request: UrlRequest?,
                    info: UrlResponseInfo?,
                    byteBuffer: ByteBuffer?
                ) {
                    byteBuffer?.let {
                        it.flip()
                        while (it.hasRemaining()) {
                            receivedData.add(it.get())
                        }
                        it.clear()
                        request?.read(it)
                    }
                }

                override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
                    try {
                        val bytes = receivedData.toByteArray()
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        continuation.resume(bitmap)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onFailed(
                    request: UrlRequest?,
                    info: UrlResponseInfo?,
                    error: CronetException?
                ) {
                    continuation.resumeWithException(error ?: Exception("Download failed"))
                }
            },
            executor
        )

        requestBuilder.build().start()
    }
}