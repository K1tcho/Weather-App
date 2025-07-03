package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var onLocationResult: ((Double, Double) -> Unit)? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                // Permission denied, show error or use default location
                onLocationResult?.invoke(51.5074, -0.1278) // London as default
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            WeatherAppTheme {
                WeatherScreen(
                    onRequestLocation = { callback ->
                        onLocationResult = callback
                        requestLocationPermission()
                    }
                )
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onLocationResult?.invoke(location.latitude, location.longitude)
                    } else {
                        // If location is null, use default location
                        onLocationResult?.invoke(51.5074, -0.1278) // London as default
                    }
                }
            }
        } catch (e: SecurityException) {
            // Handle security exception
            onLocationResult?.invoke(51.5074, -0.1278) // London as default
        }
    }
}

@Composable
fun WeatherScreen(
    onRequestLocation: ((Double, Double) -> Unit) -> Unit,
    viewModel: WeatherViewModel = viewModel(factory = WeatherViewModelFactory(WeatherRepository()))
) {
    var cityName by remember { mutableStateOf("") }
    val weatherState by viewModel.weatherState.collectAsState()
    val detailedWeatherState by viewModel.detailedWeatherState.collectAsState()

    val systemDarkTheme = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDarkTheme) }

    // Enhanced animations
    var isVisible by remember { mutableStateOf(false) }
    var isLocationLoaded by remember { mutableStateOf(false) }

    // Request location on first load
    LaunchedEffect(Unit) {
        onRequestLocation { lat, lon ->
            viewModel.getWeatherByLocation(lat, lon)
            isLocationLoaded = true
        }
        delay(500)
        isVisible = true
    }

    // Advanced background animations
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val backgroundShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing)
        ),
        label = "background_shift"
    )

    // Dynamic gradient based on weather and time
    val backgroundGradient = remember(weatherState, isDarkMode, backgroundShift) {
        createDynamicGradient(weatherState, isDarkMode, backgroundShift)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Enhanced floating particles
        EnhancedFloatingParticles(isDarkMode = isDarkMode)

        // Weather aurora effect
        WeatherAuroraEffect(weatherState = weatherState, isDarkMode = isDarkMode)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Ultra-modern header
            UltraModernHeader(
                isDarkMode = isDarkMode,
                onThemeToggle = { isDarkMode = !isDarkMode },
                isVisible = isVisible
            )

            // Enhanced search section
            EnhancedSearchSection(
                cityName = cityName,
                onCityNameChange = { cityName = it },
                onSearch = { viewModel.getWeatherByCity(cityName) },
                isDarkMode = isDarkMode,
                isVisible = isVisible
            )

            // Weather content with enhanced animations
            AnimatedContent(
                targetState = Pair(weatherState, detailedWeatherState),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(1000)) +
                            scaleIn(animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ))) togetherWith
                            (fadeOut(animationSpec = tween(500)) +
                                    scaleOut(animationSpec = tween(500)))
                },
                label = "weather_content"
            ) { (currentState, detailedState) ->
                when (currentState) {
                    is WeatherUiState.Loading -> {
                        UltraModernLoadingScreen(isDarkMode = isDarkMode)
                    }
                    is WeatherUiState.Success -> {
                        UltraModernWeatherContent(
                            weather = currentState.weather,
                            detailedWeather = detailedState,
                            isDarkMode = isDarkMode
                        )
                    }
                    is WeatherUiState.Error -> {
                        UltraModernErrorScreen(
                            message = currentState.message,
                            isDarkMode = isDarkMode
                        )
                    }
                    WeatherUiState.Initial -> {
                        if (isLocationLoaded) {
                            UltraModernLoadingScreen(isDarkMode = isDarkMode)
                        } else {
                            LocationRequestScreen(isDarkMode = isDarkMode)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UltraModernHeader(
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    isVisible: Boolean
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        ) + fadeIn(animationSpec = tween(1000))
    ) {
        UltraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            isDarkMode = isDarkMode,
            glowIntensity = 0.8f
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "WeatherLux",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Your personalized forecast",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                AnimatedContent(
                    targetState = isDarkMode,
                    transitionSpec = {
                        (scaleIn(animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )) + fadeIn()) togetherWith
                                (scaleOut() + fadeOut())
                    },
                    label = "theme_toggle"
                ) { darkMode ->
                    UltraGlassButton(
                        onClick = onThemeToggle,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            if (darkMode) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedSearchSection(
    cityName: String,
    onCityNameChange: (String) -> Unit,
    onSearch: () -> Unit,
    isDarkMode: Boolean,
    isVisible: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "search_float")
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "search_floating"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(1000))
    ) {
        UltraGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = floatingOffset.dp)
                .padding(bottom = 32.dp),
            isDarkMode = isDarkMode,
            glowIntensity = 0.6f
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UltraGlassTextField(
                    value = cityName,
                    onValueChange = onCityNameChange,
                    placeholder = "Enter city name...",
                    modifier = Modifier.weight(1f),
                    isDarkMode = isDarkMode
                )

                UltraGlassButton(
                    onClick = onSearch,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UltraModernWeatherContent(
    weather: CurrentWeatherResponse,
    detailedWeather: DetailedWeatherUiState,
    isDarkMode: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Main weather card
        MainWeatherCard(weather = weather, isDarkMode = isDarkMode)

        // Detailed info cards
        DetailedWeatherInfoCards(weather = weather, isDarkMode = isDarkMode)

        // Hourly forecast
        when (detailedWeather) {
            is DetailedWeatherUiState.Success -> {
                HourlyForecastCard(
                    hourlyWeather = detailedWeather.weather.hourly.take(24),
                    isDarkMode = isDarkMode
                )

                // Daily forecast
                DailyForecastCard(
                    dailyWeather = detailedWeather.weather.daily.take(7),
                    isDarkMode = isDarkMode
                )
            }
            is DetailedWeatherUiState.Loading -> {
                UltraGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkMode = isDarkMode
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun MainWeatherCard(
    weather: CurrentWeatherResponse,
    isDarkMode: Boolean
) {
    UltraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        glowIntensity = 1.0f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // City name and coordinates
            Text(
                text = weather.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Text(
                text = "${weather.coord.lat.roundToInt()}°, ${weather.coord.lon.roundToInt()}°",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Weather icon and temperature
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                WeatherIcon(
                    iconCode = weather.weather.firstOrNull()?.icon ?: "01d",
                    size = 80.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "${weather.main.temp.roundToInt()}°C",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.White
                    )

                    Text(
                        text = "Feels like ${weather.main.feelsLike.roundToInt()}°C",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Weather description
            Text(
                text = weather.weather.firstOrNull()?.description?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                } ?: "Unknown",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun DetailedWeatherInfoCards(
    weather: CurrentWeatherResponse,
    isDarkMode: Boolean
) {
    val weatherDetails = listOf(
        WeatherDetail("Humidity", "${weather.main.humidity}%", Icons.Rounded.WaterDrop),
        WeatherDetail("Pressure", "${weather.main.pressure} hPa", Icons.Rounded.Speed),
        WeatherDetail("Wind Speed", "${weather.wind.speed} m/s", Icons.Rounded.Air),
        WeatherDetail("Visibility", "${weather.visibility / 1000} km", Icons.Rounded.Visibility),
        WeatherDetail("Clouds", "${weather.clouds.all}%", Icons.Rounded.Cloud),
        WeatherDetail("Sunrise", formatTime(weather.sys.sunrise, weather.timezone), Icons.Rounded.WbSunny),
        WeatherDetail("Sunset", formatTime(weather.sys.sunset, weather.timezone), Icons.Rounded.Brightness3)
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(weatherDetails) { detail ->
            WeatherDetailCard(
                detail = detail,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
fun WeatherDetailCard(
    detail: WeatherDetail,
    isDarkMode: Boolean
) {
    UltraGlassCard(
        modifier = Modifier.width(120.dp),
        isDarkMode = isDarkMode,
        glowIntensity = 0.4f
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = detail.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = detail.value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = detail.label,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HourlyForecastCard(
    hourlyWeather: List<HourlyWeather>,
    isDarkMode: Boolean
) {
    UltraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        glowIntensity = 0.6f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Next 24 Hours",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(hourlyWeather) { hour ->
                    HourlyWeatherItem(
                        hourlyWeather = hour,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyWeatherItem(
    hourlyWeather: HourlyWeather,
    isDarkMode: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = formatHour(hourlyWeather.dt),
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        WeatherIcon(
            iconCode = hourlyWeather.weather.firstOrNull()?.icon ?: "01d",
            size = 32.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${hourlyWeather.temp.roundToInt()}°",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )

        if (hourlyWeather.pop > 0) {
            Text(
                text = "${(hourlyWeather.pop * 100).roundToInt()}%",
                fontSize = 10.sp,
                color = Color.Cyan.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DailyForecastCard(
    dailyWeather: List<DailyWeather>,
    isDarkMode: Boolean
) {
    UltraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        glowIntensity = 0.6f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "7-Day Forecast",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                dailyWeather.forEach { day ->
                    DailyWeatherItem(
                        dailyWeather = day,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

@Composable
fun DailyWeatherItem(
    dailyWeather: DailyWeather,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDay(dailyWeather.dt),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        WeatherIcon(
            iconCode = dailyWeather.weather.firstOrNull()?.icon ?: "01d",
            size = 32.dp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${dailyWeather.temp.max.roundToInt()}°",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )

            Text(
                text = " / ${dailyWeather.temp.min.roundToInt()}°",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun UltraModernLoadingScreen(isDarkMode: Boolean) {
    UltraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        glowIntensity = 0.8f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "loading")
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing)
                ),
                label = "loading_rotation"
            )

            Icon(
                Icons.Rounded.Cloud,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(64.dp)
                    .rotate(rotation)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Loading weather data...",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
fun UltraModernErrorScreen(
    message: String,
    isDarkMode: Boolean
) {
    UltraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        glowIntensity = 0.6f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.Error,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Oops!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Light,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LocationRequestScreen(isDarkMode: Boolean) {
    UltraGlassCard(
        modifier = Modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        glowIntensity = 0.8f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Getting your location...",
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Custom Glass Components
@Composable
fun UltraGlassCard(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    glowIntensity: Float = 0.6f,
    content: @Composable () -> Unit
) {
    val glassColor = if (isDarkMode) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    val borderColor = if (isDarkMode) {
        Color.White.copy(alpha = 0.2f)
    } else {
        Color.White.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .background(
                glassColor,
                RoundedCornerShape(24.dp)
            )
            .border(
                1.dp,
                borderColor,
                RoundedCornerShape(24.dp)
            )
    ) {
        content()
    }
}

// Complete MainActivity - Starting from UltraGlassButton composable

@Composable
fun UltraGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean,
    content: @Composable () -> Unit
) {
    val glassColor = if (isDarkMode) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }

    val borderColor = if (isDarkMode) {
        Color.White.copy(alpha = 0.3f)
    } else {
        Color.White.copy(alpha = 0.4f)
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .background(
                glassColor,
                CircleShape
            )
            .border(
                1.dp,
                borderColor,
                CircleShape
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape
    ) {
        content()
    }
}

@Composable
fun UltraGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean
) {
    val glassColor = if (isDarkMode) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .background(
                glassColor,
                RoundedCornerShape(16.dp)
            ),
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.6f)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color.White.copy(alpha = 0.4f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            cursorColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

@Composable
fun WeatherIcon(
    iconCode: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val iconResource = when (iconCode) {
        "01d" -> Icons.Rounded.WbSunny
        "01n" -> Icons.Rounded.Brightness3
        "02d", "02n" -> Icons.Rounded.WbCloudy
        "03d", "03n", "04d", "04n" -> Icons.Rounded.Cloud
        "09d", "09n" -> Icons.Rounded.Grain
        "10d", "10n" -> Icons.Rounded.WaterDrop
        "11d", "11n" -> Icons.Rounded.Thunderstorm
        "13d", "13n" -> Icons.Rounded.AcUnit
        "50d", "50n" -> Icons.Rounded.Foggy
        else -> Icons.Rounded.WbSunny
    }

    Icon(
        imageVector = iconResource,
        contentDescription = null,
        modifier = modifier.size(size),
        tint = Color.White.copy(alpha = 0.9f)
    )
}

@Composable
fun EnhancedFloatingParticles(isDarkMode: Boolean) {
    val particles = remember { List(20) { ParticleData() } }

    particles.forEach { particle ->
        val infiniteTransition = rememberInfiniteTransition(label = "particle_${particle.id}")

        val offsetY by infiniteTransition.animateFloat(
            initialValue = particle.startY,
            targetValue = particle.endY,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_y"
        )

        val offsetX by infiniteTransition.animateFloat(
            initialValue = particle.startX,
            targetValue = particle.endX,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particle_x"
        )

        Box(
            modifier = Modifier
                .offset(x = offsetX.dp, y = offsetY.dp)
                .size(particle.size.dp)
                .background(
                    Color.White.copy(alpha = particle.alpha),
                    CircleShape
                )
                .blur(1.dp)
        )
    }
}

@Composable
fun WeatherAuroraEffect(
    weatherState: WeatherUiState,
    isDarkMode: Boolean
) {
    val auroraColors = when (weatherState) {
        is WeatherUiState.Success -> {
            when (weatherState.weather.weather.firstOrNull()?.main) {
                "Rain" -> listOf(Color.Blue, Color.Cyan, Color.White)
                "Snow" -> listOf(Color.White, Color.Blue, Color.Cyan)
                "Clouds" -> listOf(Color.Gray, Color.White, Color.LightGray)
                "Clear" -> listOf(Color.Yellow, Color.Orange, Color.Red)
                else -> listOf(Color.Blue, Color.Purple, Color.Pink)
            }
        }
        else -> listOf(Color.Blue, Color.Purple, Color.Pink)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val auroraShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing)
        ),
        label = "aurora_shift"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .blur(50.dp)
    ) {
        val gradient = Brush.linearGradient(
            colors = auroraColors,
            start = Offset(0f, size.height * auroraShift),
            end = Offset(size.width, size.height * (1f - auroraShift))
        )

        drawRect(
            brush = gradient,
            alpha = 0.1f
        )
    }
}

// Helper functions
fun createDynamicGradient(
    weatherState: WeatherUiState,
    isDarkMode: Boolean,
    backgroundShift: Float
): Brush {
    val baseColors = when (weatherState) {
        is WeatherUiState.Success -> {
            when (weatherState.weather.weather.firstOrNull()?.main) {
                "Rain" -> if (isDarkMode) {
                    listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                } else {
                    listOf(Color(0xFF1976D2), Color(0xFF42A5F5), Color(0xFF90CAF9))
                }
                "Snow" -> if (isDarkMode) {
                    listOf(Color(0xFF263238), Color(0xFF455A64), Color(0xFF78909C))
                } else {
                    listOf(Color(0xFF455A64), Color(0xFF78909C), Color(0xFFB0BEC5))
                }
                "Clouds" -> if (isDarkMode) {
                    listOf(Color(0xFF37474F), Color(0xFF546E7A), Color(0xFF90A4AE))
                } else {
                    listOf(Color(0xFF546E7A), Color(0xFF90A4AE), Color(0xFFCFD8DC))
                }
                "Clear" -> if (isDarkMode) {
                    listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1976D2))
                } else {
                    listOf(Color(0xFF1565C0), Color(0xFF1976D2), Color(0xFF42A5F5))
                }
                else -> if (isDarkMode) {
                    listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3F51B5))
                } else {
                    listOf(Color(0xFF283593), Color(0xFF3F51B5), Color(0xFF5C6BC0))
                }
            }
        }
        else -> if (isDarkMode) {
            listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3F51B5))
        } else {
            listOf(Color(0xFF283593), Color(0xFF3F51B5), Color(0xFF5C6BC0))
        }
    }

    return Brush.linearGradient(
        colors = baseColors,
        start = Offset(0f, 0f),
        end = Offset(1000f * backgroundShift, 1000f * (1f - backgroundShift))
    )
}

fun formatTime(timestamp: Long, timezoneOffset: Int): String {
    val date = Date((timestamp + timezoneOffset) * 1000)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

fun formatHour(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

fun formatDay(timestamp: Long): String {
    val date = Date(timestamp * 1000)
    val formatter = SimpleDateFormat("EEE", Locale.getDefault())
    return formatter.format(date)
}

// Data classes
data class WeatherDetail(
    val label: String,
    val value: String,
    val icon: ImageVector
)

data class ParticleData(
    val id: Int = (0..1000).random(),
    val startX: Float = (0..400).random().toFloat(),
    val startY: Float = (0..800).random().toFloat(),
    val endX: Float = (0..400).random().toFloat(),
    val endY: Float = (0..800).random().toFloat(),
    val size: Float = (2..8).random().toFloat(),
    val alpha: Float = (0.1f..0.3f).random(),
    val duration: Int = (8000..15000).random()
)