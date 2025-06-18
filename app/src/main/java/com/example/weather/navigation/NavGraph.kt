package com.example.weather.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weather.screens.Splash
import com.example.weather.screens.WeatherPage
import com.example.weather.viewModel.WeatherViewModel

private const val TAG = "NavGraph"

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash.routes
    ){
        composable(Routes.Splash.routes) {
            Splash(navController)
        }

        composable(Routes.WeatherPage.routes) {
            val viewModel: WeatherViewModel = viewModel()
            WeatherPage(viewModel = viewModel)
        }
    }
}