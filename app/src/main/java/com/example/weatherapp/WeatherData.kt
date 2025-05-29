package com.example.weatherapp

data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val localtime: String
)

data class Current(
    val temp_c: Double,
    val temp_f: Double,
    val condition: Condition,
    val humidity: Int,
    val feelslike_c: Double,
    val feelslike_f: Double,
    val vis_km: Double,
    val uv: Double
)

data class Condition(
    val text: String,
    val icon: String
)
