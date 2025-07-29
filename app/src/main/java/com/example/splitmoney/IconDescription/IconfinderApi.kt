package com.example.splitmoney.IconDescription

import retrofit2.http.GET
import retrofit2.http.Query

interface IconfinderApi {
    @GET("v4/icons/search")
    suspend fun searchIcons(
        @Query("query") query: String,
        @Query("count") count: Int = 1
    ): IconfinderResponse
}