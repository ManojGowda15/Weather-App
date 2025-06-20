package com.example.weather.api

data class WeatherModel(
    val current: Current,
    val location: Location
)

data class ForecastResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day,
    val hour: List<Hour>
)

data class Day(
    val maxtemp_c: Float,
    val mintemp_c: Float,
    val daily_chance_of_rain: Int?,
    val condition: Condition
)

data class Hour(
    val time: String,
    val temp_c: Float,
    val chance_of_rain: Int?,
    val condition: Condition
)