package com.example.weather.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import coil.compose.AsyncImage
import com.example.weather.api.NetworkResponse
import com.example.weather.api.WeatherModel
import com.example.weather.viewModel.WeatherViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Info
import java.util.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed


@Composable
fun WeatherPage(viewModel: WeatherViewModel) {
    var city by remember { mutableStateOf("") }
    val weatheResult = viewModel.weatheResult.observeAsState()
    val forecastResult = viewModel.forecastResult.observeAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(city) {
        if (city.isNotBlank()) {
            viewModel.getForecast(city)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSystemInDarkTheme()) {
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), MaterialTheme.colorScheme.background)
                    } else {
                        listOf(Color(0xFFBBD2FF), Color(0xFFE6EFFF))
                    }
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 40.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Search location") },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp)
                )
                IconButton(
                    onClick = {
                        viewModel.getData(city)
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onPrimary)
                }
            }

            when (val result = weatheResult.value) {
                is NetworkResponse.Success -> {
                    val todayHours = forecastResult.value.let { forecast ->
                        if (forecast is NetworkResponse.Success) forecast.data.forecast.forecastday.firstOrNull()?.hour else null
                    }
                    val lastUpdated = result.data.location.localtime
                    WeatherDetails(result.data, todayHours)
                    // Show forecast below current weather
                    when (val forecast = forecastResult.value) {
                        is NetworkResponse.Success -> {
                            ForecastList(forecast.data.forecast.forecastday, lastUpdated)
                        }
                        is NetworkResponse.Error -> Text(
                            text = forecast.message,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                        is NetworkResponse.Loading -> CircularProgressIndicator()
                        null -> {}
                    }
                }
                is NetworkResponse.Error -> Text(
                    text = result.message,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
                is NetworkResponse.Loading -> CircularProgressIndicator()
                null -> {}
            }
        }
    }
}

@Composable
fun WeatherDetails(data: WeatherModel, todayHours: List<com.example.weather.api.Hour>?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${data.location.name}, ${data.location.country}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "${data.current.temp_c}°C",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = data.current.condition.text,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val iconUrl = when {
            data.current.condition.icon.startsWith("http") -> data.current.condition.icon
            data.current.condition.icon.startsWith("//") -> "https:${data.current.condition.icon}"
            else -> "https://cdn.weatherapi.com${data.current.condition.icon}"
        }.replace("64x64", "128x128")

        AsyncImage(
            model = iconUrl,
            contentDescription = "Weather Icon",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 8.dp)
        )

        val currentHourRainChance = todayHours?.let { hours ->
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:00", java.util.Locale.getDefault())
            val closest = hours.minByOrNull { hour ->
                val date = try { sdf.parse(hour.time) } catch (e: Exception) { null }
                if (date != null) Math.abs(date.time - now) else Long.MAX_VALUE
            }
            closest?.chance_of_rain
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Weather Today",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(5.dp))
                WeatherRow("UV", data.current.uv, "Precipitation", "${data.current.precip_mm} mm")
                WeatherRow("Humidity", "${data.current.humidity}%", "Wind Speed", "${data.current.wind_kph} km/h")
                WeatherRow("Local Time", data.location.localtime.split(" ")[1], "Local Date", data.location.localtime.split(" ")[0])

                if (currentHourRainChance != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(20.dp),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Rain chance",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Current hour rain chance: $currentHourRainChance%",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun WeatherRow(key1: String, value1: String, key2: String, value2: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WeatherKeyValue(key1, value1, modifier = Modifier.weight(1f))
        WeatherKeyValue(key2, value2, modifier = Modifier.weight(1f))
    }
}

@Composable
fun WeatherKeyValue(key: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = key,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ForecastList(forecastDays: List<com.example.weather.api.ForecastDay>, lastUpdated: String?) {
    val formattedLastUpdated = lastUpdated?.let {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(it)
            if (date != null) outputFormat.format(date) else it
        } catch (e: Exception) {
            it
        }
    } ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Weather Timeline",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Last updated: $formattedLastUpdated",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(forecastDays) { index, day ->
                ForecastCard(day, index)
            }
        }

        Text(
            text = "Disclaimer: Weather data may differ from other providers (e.g., Google Weather) due to different sources and update times.",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun ForecastCard(day: com.example.weather.api.ForecastDay, index: Int) {
    val dayLabel = when (index) {
        0 -> "Today"
        1 -> "Tomorrow"
        else -> {
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(day.date)
            val cal = Calendar.getInstance()
            cal.time = date!!
            daysOfWeek[cal.get(Calendar.DAY_OF_WEEK) - 1]
        }
    }

    val minTemp = day.day.mintemp_c.toInt()
    val maxTemp = day.day.maxtemp_c.toInt()
    val rainChance = day.day.daily_chance_of_rain ?: 0
    val iconUrl = when {
        day.day.condition.icon.startsWith("http") -> day.day.condition.icon
        day.day.condition.icon.startsWith("//") -> "https:${day.day.condition.icon}"
        else -> "https://cdn.weatherapi.com${day.day.condition.icon}"
    }.replace("64x64", "128x128")
    val conditionText = day.day.condition.text

    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(220.dp)
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayLabel,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = day.date,
                fontSize = 14.sp,
                color = Color.Gray
            )
            AsyncImage(
                model = iconUrl,
                contentDescription = "Weather Icon",
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                text = conditionText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Rain: $rainChance%",
                fontSize = 14.sp,
                color = Color(0xFF64B5F6)
            )
            Text(
                text = "Min: $minTemp°  Max: $maxTemp°",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
