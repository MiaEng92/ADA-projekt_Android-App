package com.example.mysurveyapp.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val USE_MOCK = true
    private const val MOCK_URL = "http://10.0.2.2:8001/"
    private const val REAL_URL = "http://10.0.2.2:5142/"
    private val BASE_URL = if (USE_MOCK) MOCK_URL else REAL_URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
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