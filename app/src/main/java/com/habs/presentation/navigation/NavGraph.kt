package com.habs.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.habs.presentation.calendar.CalendarScreen
import com.habs.presentation.settings.SettingsScreen
import com.habs.presentation.stats.StatsScreen
import com.habs.presentation.today.TodayScreen

sealed class Screen(val route: String) {
    object Today : Screen("today")
    object Stats : Screen("stats")
    object Calendar : Screen("calendar")
    object Settings : Screen("settings")
}

@Composable
fun HabsNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Today.route) {
        composable(Screen.Today.route) {
            TodayScreen(
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCalendar = { navController.navigate(Screen.Calendar.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStats = {
                    if (!navController.popBackStack(Screen.Stats.route, false)) {
                        navController.navigate(Screen.Stats.route)
                    }
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}