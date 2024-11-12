package com.example.appnews

import android.content.Context
import com.google.gson.Gson
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CronetClient(context: Context) {
    private val cronetEngine: CronetEngine
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val gson = Gson()

    init {
        cronetEngine = CronetEngine.Builder(context)
            .enableHttp2(true)
            .enableQuic(true)
            .build()
    }

    suspend fun fetchSportsNews(sport: String, apiKey: String): NewsResponse {
        val url = "https://newsapi.org/v2/everything?q=$sport&apiKey=$apiKey&language=en&sortBy=publishedAt"

        return suspendCoroutine { continuation ->
            val requestBuilder = cronetEngine.newUrlRequestBuilder(
                url,
                object : UrlRequest.Callback() {
                    private val receivedData = StringBuilder()

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
                            continuation.resumeWithException(
                                Exception("HTTP Error: ${info?.httpStatusCode}")
                            )
                        }
                    }

                    override fun onReadCompleted(
                        request: UrlRequest?,
                        info: UrlResponseInfo?,
                        byteBuffer: ByteBuffer?
                    ) {
                        byteBuffer?.let {
                            it.flip()
                            val bytes = ByteArray(it.remaining())
                            it.get(bytes)
                            receivedData.append(String(bytes))
                            it.clear()
                            request?.read(it)
                        }
                    }

                    override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
                        try {
                            val response = gson.fromJson(receivedData.toString(), NewsResponse::class.java)
                            continuation.resume(response)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }

                    override fun onFailed(
                        request: UrlRequest?,
                        info: UrlResponseInfo?,
                        error: CronetException?
                    ) {
                        continuation.resumeWithException(
                            error ?: Exception("Request failed")
                        )
                    }
                },
                executor
            )

            requestBuilder
                .addHeader("Content-Type", "application/json")
                .build()
                .start()
        }
    }
}