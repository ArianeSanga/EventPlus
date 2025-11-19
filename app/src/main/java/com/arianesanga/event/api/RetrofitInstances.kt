package com.arianesanga.event.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstances {

    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    val geocodingApi: GeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/geo/1.0/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingApiService::class.java)
    }
}