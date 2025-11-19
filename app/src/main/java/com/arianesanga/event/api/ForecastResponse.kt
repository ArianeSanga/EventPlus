package com.arianesanga.event.api

data class ForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: ForecastMain,
    val weather: List<WeatherDesc>,
    val wind: ForecastWind
)

data class ForecastMain(
    val temp: Float,
    val feels_like: Float,
    val temp_min: Float,
    val temp_max: Float,
    val pressure: Int,
    val humidity: Int
)

data class WeatherDesc(
    val main: String,
    val description: String,
    val icon: String
)

data class City(
    val name: String,
    val country: String
)

data class ForecastWind(
    val speed: Float,
    val deg: Int
)