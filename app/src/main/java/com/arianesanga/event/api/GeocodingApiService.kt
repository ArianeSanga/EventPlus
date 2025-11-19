package com.arianesanga.event.api

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {

    @GET("direct")
    suspend fun getCoordinates(
        @Query("q") city: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeocodingResult>
}