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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

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

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel(factory = WeatherViewModelFactory(WeatherRepository()))) {
    var cityName by remember { mutableStateOf("") }
    val weatherState by viewModel.weatherState.collectAsState()

    // Fix: Get the system theme state first, then use it in remember
    val systemDarkTheme = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkTheme) }

    // Enhanced animation states
    var isVisible by remember { mutableStateOf(false) }
    val animatedVisibilityState = animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "visibility"
    )

    // Floating animation for glass cards
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // Load default weather with entrance animation
    LaunchedEffect(Unit) {
        viewModel.getWeather("London")
        delay(500)
        isVisible = true
    }

    // Enhanced dynamic gradient based on weather condition
    val backgroundGradient = when (weatherState) {
        is WeatherUiState.Success -> {
            val condition = (weatherState as WeatherUiState.Success).weather.current.condition.text.lowercase()
            if (isDarkMode) {
                when {
                    condition.contains("sunny") || condition.contains("clear") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243e)
                        ))
                    condition.contains("cloud") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF232526), Color(0xFF414345), Color(0xFF2C2C54)
                        ))
                    condition.contains("rain") || condition.contains("drizzle") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF0B0C10), Color(0xFF1F2833), Color(0xFF2E4057)
                        ))
                    condition.contains("snow") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF2E3192), Color(0xFF1BFFFF), Color(0xFF4A69BD)
                        ))
                    condition.contains("thunder") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF000000), Color(0xFF434343), Color(0xFF1A1A2E)
                        ))
                    else ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F0C29)
                        ))
                }
            } else {
                when {
                    condition.contains("sunny") || condition.contains("clear") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF4FACFE), Color(0xFF00F2FE), Color(0xFF43CBFF)
                        ))
                    condition.contains("cloud") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF667eea), Color(0xFF764ba2), Color(0xFF8360c3)
                        ))
                    condition.contains("rain") || condition.contains("drizzle") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF4b6cb7), Color(0xFF182848), Color(0xFF2E4057)
                        ))
                    condition.contains("snow") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFFe6ddd4), Color(0xFF9BB5FF), Color(0xFF74b9ff)
                        ))
                    condition.contains("thunder") ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF2F1B69), Color(0xFF8E44AD), Color(0xFF9b59b6)
                        ))
                    else ->
                        Brush.verticalGradient(listOf(
                            Color(0xFF667eea), Color(0xFF764ba2), Color(0xFF667eea)
                        ))
                }
            }
        }
        else -> if (isDarkMode) {
            Brush.verticalGradient(listOf(
                Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F0C29)
            ))
        } else {
            Brush.verticalGradient(listOf(
                Color(0xFF667eea), Color(0xFF764ba2), Color(0xFF667eea)
            ))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Animated background particles
        AnimatedBackgroundParticles(isDarkMode = isDarkMode)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
                .scale(animatedVisibilityState.value)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Enhanced header with glassmorphism
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                isDarkMode = isDarkMode
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Weather",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Your daily forecast",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    AnimatedContent(
                        targetState = isDarkMode,
                        transitionSpec = {
                            (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut())
                        },
                        label = "theme_toggle"
                    ) { darkMode ->
                        GlassButton(
                            onClick = { isDarkMode = !isDarkMode },
                            isDarkMode = isDarkMode
                        ) {
                            Icon(
                                if (darkMode) Icons.Default.WbSunny else Icons.Default.Bedtime,
                                contentDescription = "Toggle theme",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Enhanced search section with glassmorphism
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                        .offset(y = (floatingOffset * 0.5f).dp),
                    isDarkMode = isDarkMode
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = cityName,
                            onValueChange = { cityName = it },
                            label = {
                                Text(
                                    "Search city",
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(15.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White.copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White.copy(alpha = 0.8f),
                                cursorColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        val searchButtonScale by animateFloatAsState(
                            targetValue = if (cityName.isNotBlank()) 1.1f else 1f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "search_scale"
                        )

                        GlassButton(
                            onClick = {
                                if (cityName.isNotBlank()) {
                                    viewModel.getWeather(cityName)
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .scale(searchButtonScale),
                            isDarkMode = isDarkMode
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

            // Weather Content with enhanced animations
            AnimatedContent(
                targetState = weatherState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(800)) +
                            scaleIn(animationSpec = tween(800))) togetherWith
                            (fadeOut(animationSpec = tween(400)) +
                                    scaleOut(animationSpec = tween(400)))
                },
                label = "weather_content"
            ) { state ->
                when (state) {
                    is WeatherUiState.Loading -> {
                        EnhancedLoadingAnimation(isDarkMode = isDarkMode)
                    }

                    is WeatherUiState.Success -> {
                        EnhancedWeatherContent(
                            weather = state.weather,
                            isDarkMode = isDarkMode,
                            floatingOffset = floatingOffset
                        )
                    }

                    is WeatherUiState.Error -> {
                        EnhancedErrorCard(message = state.message, isDarkMode = isDarkMode)
                    }

                    WeatherUiState.Initial -> TODO()
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDarkMode) 0.1f else 0.2f),
                        Color.White.copy(alpha = if (isDarkMode) 0.05f else 0.1f)
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.clip(CircleShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = if (isDarkMode) 0.15f else 0.25f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        content()
    }
}

@Composable
fun AnimatedBackgroundParticles(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    // Multiple floating particles with different speeds
    repeat(5) { index ->
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween((3000 + index * 500), easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particle_$index"
        )

        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween((8000 + index * 1000), easing = LinearEasing)
            ),
            label = "particle_rotation_$index"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(
                    x = (50 + index * 60).dp,
                    y = (100 + index * 80 + offsetY).dp
                )
        ) {
            Box(
                modifier = Modifier
                    .size((20 + index * 5).dp)
                    .rotate(rotation)
                    .clip(CircleShape)
                    .background(
                        Color.White.copy(alpha = if (isDarkMode) 0.05f else 0.1f)
                    )
                    .blur(2.dp)
            )
        }
    }
}

@Composable
fun EnhancedLoadingAnimation(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing)
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(isDarkMode = isDarkMode) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.6f),
                                        Color.Transparent
                                    ),
                                    center = Offset(0.5f, 0.5f)
                                )
                            )
                            .rotate(shimmer * 360f)
                    )

                    // Inner circle
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Text(
                            "🌤️",
                            fontSize = 24.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Fetching weather data...",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EnhancedErrorCard(message: String, isDarkMode: Boolean) {
    val shake by rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(150),
            repeatMode = RepeatMode.Reverse
        )
    )

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = shake.dp),
        isDarkMode = isDarkMode
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated error icon
            Text(
                "⚠️",
                fontSize = 64.sp,
                modifier = Modifier.scale(
                    animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "error_scale"
                    ).value
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Oops! Unable to fetch weather",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                message,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun EnhancedWeatherContent(
    weather: WeatherResponse,
    isDarkMode: Boolean,
    floatingOffset: Float
) {
    var isContentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(weather) {
        isContentVisible = false
        delay(200)
        isContentVisible = true
    }

    AnimatedVisibility(
        visible = isContentVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn()
    ) {
        Column {
            // Main Weather Card with enhanced glassmorphism
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .offset(y = floatingOffset.dp),
                isDarkMode = isDarkMode
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Location with enhanced styling
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "${weather.location.name}, ${weather.location.country}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Text(
                        weather.location.localtime,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Enhanced Weather icon with better animations
                    EnhancedWeatherIcon(
                        condition = weather.current.condition.text,
                        isDarkMode = isDarkMode
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Temperature with better styling
                    val temperatureAnimation by animateFloatAsState(
                        targetValue = weather.current.temp_c.toFloat(),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "temperature"
                    )

                    Text(
                        "${temperatureAnimation.toInt()}°",
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color.White
                    )

                    Text(
                        weather.current.condition.text,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "Feels like ${weather.current.feelslike_c.toInt()}°C",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Enhanced Weather Details Grid
            val detailCards = listOf(
                Triple("Humidity", "${weather.current.humidity}%", "💧"),
                Triple("Visibility", "${weather.current.vis_km.toInt()} km", "👁️"),
                Triple("UV Index", weather.current.uv.toInt().toString(), "☀️"),
                Triple("Feels Like", "${weather.current.feelslike_c.toInt()}°C", "🌡️")
            )

            detailCards.chunked(2).forEachIndexed { rowIndex, rowCards ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowCards.forEachIndexed { cardIndex, (title, value, emoji) ->
                        AnimatedVisibility(
                            visible = isContentVisible,
                            enter = slideInHorizontally(
                                initialOffsetX = { if (cardIndex == 0) -it else it },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + fadeIn(
                                animationSpec = tween(800)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            EnhancedWeatherDetailCard(
                                title = title,
                                value = value,
                                emoji = emoji,
                                isDarkMode = isDarkMode,
                                offset = (floatingOffset * (0.3f + cardIndex * 0.1f)).dp
                            )
                        }
                    }
                }

                if (rowIndex == 0) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun EnhancedWeatherIcon(condition: String, isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "weather_icon")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (condition.lowercase().contains("sunny")) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "sun_rotation"
    )

    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    val glow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .size(120.dp)
            .offset(y = bounce.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background effect
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.3f * glow),
                            Color.White.copy(alpha = 0.1f * glow),
                            Color.Transparent
                        )
                    )
                )
        )

        // Weather icon
        Text(
            getEnhancedWeatherEmoji(condition),
            fontSize = 64.sp,
            modifier = Modifier.rotate(rotation)
        )
    }
}

@Composable
fun EnhancedWeatherDetailCard(
    title: String,
    value: String,
    emoji: String,
    isDarkMode: Boolean,
    offset: Dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    GlassCard(
        modifier = Modifier
            .scale(scale)
            .offset(y = offset),
        isDarkMode = isDarkMode
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterVertically as Alignment.Horizontal
        ) {
            Text(
                emoji,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun getEnhancedWeatherEmoji(condition: String): String {
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