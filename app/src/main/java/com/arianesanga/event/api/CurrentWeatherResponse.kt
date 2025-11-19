package com.arianesanga.event.api

data class CurrentWeatherResponse(
    val weather: List<WeatherDesc>,
    val main: WeatherMain,
    val name: String
)

data class WeatherMain(
    val temp: Float,
    val humidity: Int
)