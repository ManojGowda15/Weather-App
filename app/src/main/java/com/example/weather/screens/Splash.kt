package com.example.weather.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.weather.R
import com.example.weather.navigation.Routes
import kotlinx.coroutines.delay

@Composable
fun Splash(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(500)
        navController.navigate(Routes.WeatherPage.routes) {
            popUpTo(Routes.Splash.routes) {
                inclusive = true
            }
        }
    }
    SplashContent()
}

@Composable
private fun SplashContent() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = "Splash Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}