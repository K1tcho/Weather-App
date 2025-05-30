package com.example.weatherapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class WeatherRepository {
    private val apiKey = "44c2e1c4e6fa4a02b1d02336241611"
    private val baseUrl = "https://api.weatherapi.com/v1/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiService::class.java)

    suspend fun getCurrentWeather(location: String): Result<WeatherResponse> {
        return try {
            val response = api.getCurrentWeather(apiKey, location)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid location. Please check the city name."
                    401 -> "API key is invalid."
                    403 -> "API key quota exceeded."
                    404 -> "Location not found."
                    else -> "Failed to fetch weather data: ${response.message()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Connection timeout. Please check your internet connection."
                e.message?.contains("unable to resolve host", ignoreCase = true) == true ->
                    "No internet connection. Please check your network."
                else -> e.message ?: "Unknown error occurred"
            }
            Result.failure(Exception(errorMessage))
        }
    }
}