package com.example.mysurveyapp.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // true = test-serveren i PyCharm (port 8001)
    // false = det rigtige backend (port 5142)
    const val USE_MOCK = false

    private const val MOCK_URL = "http://10.0.2.2:8001/"
    private const val REAL_URL = "http://10.0.2.2:5142/"
    // Her vælges IP-adressen når appen starter
    private val BASE_URL = if (USE_MOCK) MOCK_URL else REAL_URL

    // Sender tokenet med i hvert kald, når man er logget ind
    private val authInterceptor = Interceptor { chain ->
        val token = TokenStore.token
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        chain.proceed(request)
    }
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        redactHeader("Authorization")
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    val api: AdaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdaApiService::class.java)
    }
}