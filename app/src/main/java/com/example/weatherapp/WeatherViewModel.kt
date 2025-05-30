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

    fun getWeather(location: String) {
        if (location.isBlank()) {
            _weatherState.value = WeatherUiState.Error("Please enter a valid location")
            return
        }

        viewModelScope.launch {
            try {
                _weatherState.value = WeatherUiState.Loading
                repository.getCurrentWeather(location.trim())
                    .onSuccess { weather ->
                        _weatherState.value = WeatherUiState.Success(weather)
                    }
                    .onFailure { error ->
                        _weatherState.value = WeatherUiState.Error(
                            error.message ?: "Failed to fetch weather data"
                        )
                    }
            } catch (e: Exception) {
                _weatherState.value = WeatherUiState.Error(
                    "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun retryLastSearch() {
        // This could be improved by storing the last searched location
        getWeather("London") // Default fallback
    }
}

sealed class WeatherUiState {
    object Initial : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
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