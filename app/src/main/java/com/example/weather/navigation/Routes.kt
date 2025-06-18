package com.example.weather.navigation

sealed class Routes(val routes: String) {
    object Splash: Routes("splash")
    object WeatherPage: Routes("weather-page")
}