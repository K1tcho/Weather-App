package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                WeatherScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(factory = WeatherViewModelFactory(WeatherRepository()))) {
    var cityName by remember { mutableStateOf("") }
    val weatherState by viewModel.weatherState.collectAsState()
    var isDarkMode by remember { mutableStateOf(isSystemInDarkTheme()) }

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    val animatedVisibilityState = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "visibility"
    )

    // Load default weather with entrance animation
    LaunchedEffect(Unit) {
        viewModel.getWeather("London")
        delay(300)
        isVisible = true
    }

    // Dynamic gradient based on weather condition and theme
    val backgroundGradient = when (weatherState) {
        is WeatherUiState.Success -> {
            val condition = (weatherState as WeatherUiState.Success).weather.current.condition.text.lowercase()
            if (isDarkMode) {
                when {
                    condition.contains("sunny") || condition.contains("clear") ->
                        Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
                    condition.contains("cloud") ->
                        Brush.verticalGradient(listOf(Color(0xFF2C3E50), Color(0xFF34495E)))
                    condition.contains("rain") || condition.contains("drizzle") ->
                        Brush.verticalGradient(listOf(Color(0xFF1B263B), Color(0xFF2A3D66)))
                    condition.contains("snow") ->
                        Brush.verticalGradient(listOf(Color(0xFF0F3460), Color(0xFF16537E)))
                    condition.contains("thunder") ->
                        Brush.verticalGradient(listOf(Color(0xFF0C0C0C), Color(0xFF1A1A1A)))
                    else ->
                        Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
                }
            } else {
                when {
                    condition.contains("sunny") || condition.contains("clear") ->
                        Brush.verticalGradient(listOf(Color(0xFF87CEEB), Color(0xFF98D8E8)))
                    condition.contains("cloud") ->
                        Brush.verticalGradient(listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD)))
                    condition.contains("rain") || condition.contains("drizzle") ->
                        Brush.verticalGradient(listOf(Color(0xFF607D8B), Color(0xFF90A4AE)))
                    condition.contains("snow") ->
                        Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)))
                    condition.contains("thunder") ->
                        Brush.verticalGradient(listOf(Color(0xFF37474F), Color(0xFF546E7A)))
                    else ->
                        Brush.verticalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3)))
                }
            }
        }
        else -> if (isDarkMode) {
            Brush.verticalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
        } else {
            Brush.verticalGradient(listOf(Color(0xFF6200EE), Color(0xFF3700B3)))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .scale(animatedVisibilityState.value)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header with theme toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Weather",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.White
                )

                AnimatedContent(
                    targetState = isDarkMode,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "theme_toggle"
                ) { darkMode ->
                    IconButton(
                        onClick = { isDarkMode = !isDarkMode },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Search Section with animation
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(800, delayMillis = 200)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkMode)
                            Color.White.copy(alpha = 0.1f)
                        else
                            Color.White.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = cityName,
                            onValueChange = { cityName = it },
                            label = {
                                Text(
                                    "Search city",
                                    color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF666666)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6200EE),
                                unfocusedBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.3f) else Color(0xFFE0E0E0),
                                focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                                unfocusedTextColor = if (isDarkMode) Color.White else Color.Black
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        val searchButtonScale by animateFloatAsState(
                            targetValue = if (cityName.isNotBlank()) 1.1f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "search_scale"
                        )

                        FloatingActionButton(
                            onClick = {
                                if (cityName.isNotBlank()) {
                                    viewModel.getWeather(cityName)
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .scale(searchButtonScale),
                            containerColor = Color(0xFF6200EE)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Weather Content with animations
            AnimatedContent(
                targetState = weatherState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(600)) + scaleIn(animationSpec = tween(600)) togetherWith
                            fadeOut(animationSpec = tween(300)) + scaleOut(animationSpec = tween(300))
                },
                label = "weather_content"
            ) { state ->
                when (state) {
                    is WeatherUiState.Loading -> {
                        LoadingAnimation(isDarkMode = isDarkMode)
                    }

                    is WeatherUiState.Success -> {
                        WeatherContent(weather = state.weather, isDarkMode = isDarkMode)
                    }

                    is WeatherUiState.Error -> {
                        ErrorCard(message = state.message, isDarkMode = isDarkMode)
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingAnimation(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode)
                    Color.White.copy(alpha = 0.1f)
                else
                    Color.White.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .rotate(rotation)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF6200EE),
                        strokeWidth = 4.dp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Loading weather...",
                    fontSize = 16.sp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun ErrorCard(message: String, isDarkMode: Boolean) {
    val shake by rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(100),
            repeatMode = RepeatMode.Reverse
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shake.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode)
                Color(0xFF4A0E0E).copy(alpha = 0.3f)
            else
                Color(0xFFFFEBEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "⚠️",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Oops! Something went wrong",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFFF6B6B) else Color(0xFFD32F2F)
            )
            Text(
                message,
                fontSize = 14.sp,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun WeatherContent(weather: WeatherResponse, isDarkMode: Boolean) {
    var isContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(weather) {
        isContentVisible = false
        delay(100)
        isContentVisible = true
    }

    AnimatedVisibility(
        visible = isContentVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(800)
        ) + fadeIn(animationSpec = tween(800))
    ) {
        Column {
            // Main Weather Card with entrance animation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(25.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode)
                        Color.White.copy(alpha = 0.1f)
                    else
                        Color.White.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Location with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color(0xFF6200EE),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${weather.location.name}, ${weather.location.country}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDarkMode) Color.White else Color(0xFF333333)
                        )
                    }

                    Text(
                        weather.location.localtime,
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Animated Weather icon
                    AnimatedWeatherIcon(
                        condition = weather.current.condition.text,
                        isDarkMode = isDarkMode
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Temperature
                    val temperatureAnimation by animateFloatAsState(
                        targetValue = weather.current.temp_c.toFloat(),
                        animationSpec = tween(durationMillis = 1000),
                        label = "temperature"
                    )

                    Text(
                        "${temperatureAnimation.toInt()}°",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        color = if (isDarkMode) Color.White else Color(0xFF333333)
                    )

                    Text(
                        weather.current.condition.text,
                        fontSize = 18.sp,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "Feels like ${weather.current.feelslike_c.toInt()}°C",
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF999999)
                    )
                }
            }

            // Weather Details Grid with staggered animation
            val detailCards = listOf(
                Triple("Humidity", "${weather.current.humidity}%", "💧"),
                Triple("Visibility", "${weather.current.vis_km.toInt()} km", "👁️"),
                Triple("UV Index", weather.current.uv.toInt().toString(), "☀️"),
                Triple("Temperature", "${weather.current.temp_f.toInt()}°F", "🌡️")
            )

            detailCards.chunked(2).forEachIndexed { rowIndex, rowCards ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowCards.forEachIndexed { cardIndex, (title, value, emoji) ->
                        AnimatedVisibility(
                            visible = isContentVisible,
                            enter = slideInHorizontally(
                                initialOffsetX = { if (cardIndex == 0) -it else it },
                                animationSpec = tween(600, delayMillis = (rowIndex * 2 + cardIndex) * 100)
                            ) + fadeIn(animationSpec = tween(600, delayMillis = (rowIndex * 2 + cardIndex) * 100)),
                            modifier = Modifier.weight(1f)
                        ) {
                            WeatherDetailCard(
                                title = title,
                                value = value,
                                emoji = emoji,
                                isDarkMode = isDarkMode
                            )
                        }
                    }
                }

                if (rowIndex == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AnimatedWeatherIcon(condition: String, isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_icon")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (condition.lowercase().contains("sunny")) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing)
        ),
        label = "sun_rotation"
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(
                if (isDarkMode) {
                    Brush.radialGradient(
                        listOf(Color(0xFF4A90E2), Color(0xFF357ABD))
                    )
                } else {
                    Brush.radialGradient(
                        listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                    )
                }
            )
            .offset(y = bounce.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            getWeatherEmoji(condition),
            fontSize = 48.sp,
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
fun WeatherDetailCard(
    title: String,
    value: String,
    emoji: String,
    isDarkMode: Boolean
) {
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    Card(
        modifier = Modifier.scale(scale),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode)
                Color.White.copy(alpha = 0.1f)
            else
                Color.White.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                title,
                fontSize = 12.sp,
                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF666666),
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color(0xFF333333),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

fun getWeatherEmoji(condition: String): String {
    return when {
        condition.lowercase().contains("sunny") || condition.lowercase().contains("clear") -> "☀️"
        condition.lowercase().contains("partly cloudy") -> "⛅"
        condition.lowercase().contains("cloudy") || condition.lowercase().contains("overcast") -> "☁️"
        condition.lowercase().contains("rain") || condition.lowercase().contains("drizzle") -> "🌧️"
        condition.lowercase().contains("thunder") -> "⛈️"
        condition.lowercase().contains("snow") -> "❄️"
        condition.lowercase().contains("fog") || condition.lowercase().contains("mist") -> "🌫️"
        condition.lowercase().contains("wind") -> "💨"
        else -> "🌤️"
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    WeatherAppTheme {
        WeatherScreen()
    }
}