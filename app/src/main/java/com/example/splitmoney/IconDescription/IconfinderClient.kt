package com.example.splitmoney.IconDescription

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object IconfinderClient {
    private const val BASE_URL = "https://api.iconfinder.com/"
    private const val TOKEN = "Tc6TYjlfHXmNBWFaztKU8c5AuWq8g2b5aUhhSQf6PTYGjJJYfPT3LgnNKnfcHYqo" // Replace this with your real token

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $TOKEN")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: IconfinderApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(IconfinderApi::class.java)
}