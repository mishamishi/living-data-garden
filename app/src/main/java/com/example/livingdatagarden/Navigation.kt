package com.example.livingdatagarden

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Loading : Screen("loading")
    object Home : Screen("home")
    object Dashboard : Screen("dashboard")
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Screen.Loading.route) {

        composable(Screen.Loading.route) {
            LoadingScreen(
                onFinished = {
                    nav.navigate(Screen.Home.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onDashboard = { nav.navigate(Screen.Dashboard.route) }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onHome = { nav.popBackStack() }
            )
        }
    }
}