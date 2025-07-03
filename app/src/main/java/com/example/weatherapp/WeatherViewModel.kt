package com.example.weatherapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Initial)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    private val _detailedWeatherState = MutableStateFlow<DetailedWeatherUiState>(DetailedWeatherUiState.Initial)
    val detailedWeatherState: StateFlow<DetailedWeatherUiState> = _detailedWeatherState.asStateFlow()

    fun getWeatherByLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                _weatherState.value = WeatherUiState.Loading
                _detailedWeatherState.value = DetailedWeatherUiState.Loading

                repository.getCurrentWeather(lat, lon)
                    .onSuccess { weather ->
                        _weatherState.value = WeatherUiState.Success(weather)
                        // Also fetch detailed weather data
                        getDetailedWeather(lat, lon)
                    }
                    .onFailure { error ->
                        _weatherState.value = WeatherUiState.Error(
                            error.message ?: "Failed to fetch weather data"
                        )
                        _detailedWeatherState.value = DetailedWeatherUiState.Error(
                            error.message ?: "Failed to fetch detailed weather data"
                        )
                    }
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
                _detailedWeatherState.value = DetailedWeatherUiState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun getWeatherByCity(cityName: String) {
        if (cityName.isBlank()) {
            _weatherState.value = WeatherUiState.Error("Please enter a valid city name")
            return
        }

        viewModelScope.launch {
            try {
                _weatherState.value = WeatherUiState.Loading
                _detailedWeatherState.value = DetailedWeatherUiState.Loading

                repository.getCurrentWeatherByCity(cityName.trim())
                    .onSuccess { weather ->
                        _weatherState.value = WeatherUiState.Success(weather)
                        // Also fetch detailed weather data using coordinates
                        getDetailedWeather(weather.coord.lat, weather.coord.lon)
                    }
                    .onFailure { error ->
                        _weatherState.value = WeatherUiState.Error(
                            error.message ?: "Failed to fetch weather data"
                        )
                        _detailedWeatherState.value = DetailedWeatherUiState.Error(
                            error.message ?: "Failed to fetch detailed weather data"
                        )
                    }
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
                _detailedWeatherState.value = DetailedWeatherUiState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    private fun getDetailedWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                repository.getOneCallWeather(lat, lon)
                    .onSuccess { detailedWeather ->
                        _detailedWeatherState.value = DetailedWeatherUiState.Success(detailedWeather)
                    }
                    .onFailure { error ->
                        _detailedWeatherState.value = DetailedWeatherUiState.Error(
                            error.message ?: "Failed to fetch detailed weather data"
                        )
                    }
            } catch (e: Exception) {
                _detailedWeatherState.value = DetailedWeatherUiState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun retryLastSearch() {
        // Use London as default fallback
        getWeatherByLocation(51.5074, -0.1278)
    }
}

sealed class WeatherUiState {
    object Initial : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val weather: CurrentWeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

sealed class DetailedWeatherUiState {
    object Initial : DetailedWeatherUiState()
    object Loading : DetailedWeatherUiState()
    data class Success(val weather: OneCallResponse) : DetailedWeatherUiState()
    data class Error(val message: String) : DetailedWeatherUiState()
}

class WeatherViewModelFactory(private val repository: WeatherRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}