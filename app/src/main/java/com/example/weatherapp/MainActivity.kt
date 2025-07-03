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