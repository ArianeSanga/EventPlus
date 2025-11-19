package com.arianesanga.event.data.remote.repository

import com.arianesanga.event.api.CurrentWeatherResponse
import com.arianesanga.event.api.ForecastResponse
import com.arianesanga.event.api.GeocodingResult
import com.arianesanga.event.api.RetrofitInstances

class WeatherRepository {

    suspend fun getCoordinates(city: String, apiKey: String): GeocodingResult? {
        return RetrofitInstances.geocodingApi
            .getCoordinates(city = city, apiKey = apiKey)
            .firstOrNull()
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double, apiKey: String): CurrentWeatherResponse {
        return RetrofitInstances.weatherApi
            .getWeatherByLatLon(lat, lon, apiKey)
    }

    suspend fun getForecast(lat: Double, lon: Double, apiKey: String): ForecastResponse {
        return RetrofitInstances.weatherApi.getForecast(lat, lon, apiKey)
    }
}