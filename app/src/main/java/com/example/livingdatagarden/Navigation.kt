package com.example.livingdatagarden

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
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
    // One ViewModel shared by every screen.
    val vm: GardenViewModel = viewModel()
    val state by vm.state.collectAsState()

    NavHost(navController = nav, startDestination = Screen.Loading.route) {

        composable(Screen.Loading.route) {
            LoadingScreen(
                state = state,
                onFinished = {
                    nav.navigate(Screen.Home.route) {
                        popUpTo(Screen.Loading.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                state = state,
                onRefresh = vm::refresh,
                onSetLive = vm::setLive,
                onDashboard = { nav.navigate(Screen.Dashboard.route) }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                state = state,
                onRefresh = vm::refresh,
                onHome = { nav.popBackStack() }
            )
        }
    }
}