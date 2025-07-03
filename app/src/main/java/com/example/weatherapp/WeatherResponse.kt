package com.example.weatherapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class WeatherRepository {
    private val apiKey = "7ec5b95a0eaf160b7060dc9fcc68dede"
    private val baseUrl = "https://api.openweathermap.org/data/2.5/"

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

    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<CurrentWeatherResponse> {
        return try {
            val response = api.getCurrentWeather(lat, lon, apiKey)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid coordinates provided."
                    401 -> "API key is invalid."
                    403 -> "API key quota exceeded."
                    404 -> "Location not found."
                    429 -> "API rate limit exceeded."
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

    suspend fun getCurrentWeatherByCity(cityName: String): Result<CurrentWeatherResponse> {
        return try {
            val response = api.getCurrentWeatherByCity(cityName, apiKey)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid city name. Please check the spelling."
                    401 -> "API key is invalid."
                    403 -> "API key quota exceeded."
                    404 -> "City not found. Please check the city name."
                    429 -> "API rate limit exceeded."
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

    suspend fun getOneCallWeather(lat: Double, lon: Double): Result<OneCallResponse> {
        return try {
            val response = api.getOneCallWeather(lat, lon, apiKey)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Invalid coordinates provided."
                    401 -> "API key is invalid."
                    403 -> "API key quota exceeded."
                    404 -> "Location not found."
                    429 -> "API rate limit exceeded."
                    else -> "Failed to fetch detailed weather data: ${response.message()}"
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