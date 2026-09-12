package com.example.core.network

import com.example.core.security.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // Current Cloud Run deployment host for Privo backend
    const val BASE_URL = "https://ais-dev-czkhglys2igce2wspciorl-436229882791.us-west1.run.app"
    const val WS_URL = "wss://ais-dev-czkhglys2igce2wspciorl-436229882791.us-west1.run.app/ws"

    private var apiService: PrivoApiService? = null
    private var sessionManager: SessionManager? = null

    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    fun init(sessionManager: SessionManager) {
        this.sessionManager = sessionManager
    }

    val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            sessionManager?.getSessionId()?.let { sessionId ->
                requestBuilder.header("X-Session-Id", sessionId)
                requestBuilder.header("Authorization", "Bearer $sessionId")
            }
            requestBuilder.header("Content-Type", "application/json")
            requestBuilder.header("Accept", "application/json")
            requestBuilder.header("User-Agent", "PrivoAndroidNative/1.0")

            chain.proceed(requestBuilder.build())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getService(): PrivoApiService {
        return apiService ?: synchronized(this) {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(PrivoApiService::class.java)
            apiService = service
            service
        }
    }
}
