package com.example.weatherapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository {
    private val apiKey = "44c2e1c4e6fa4a02b1d02336241611"
    private val baseUrl = "https://api.weatherapi.com/v1/"

    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiService::class.java)

    suspend fun getCurrentWeather(location: String): Result<WeatherResponse> {
        return try {
            val response = api.getCurrentWeather(apiKey, location)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch weather data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
