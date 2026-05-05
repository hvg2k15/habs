package com.habs.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.habs.presentation.settings.SettingsScreen
import com.habs.presentation.stats.HabitStatsDetailScreen
import com.habs.presentation.stats.HabitStatsHeatmapScreen
import com.habs.presentation.stats.StatsScreen
import com.habs.presentation.today.TodayFullListScreen
import com.habs.presentation.today.TodayScreen

sealed class Screen(val route: String) {
    object Today : Screen("today")
    object TodayFull : Screen("today_full")
    object Stats : Screen("stats")
    object HabitStats : Screen("stats_habit/{habitId}") {
        fun createRoute(habitId: Long) = "stats_habit/$habitId"
    }
    object HabitStatsHeatmap : Screen("stats_habit_heatmap/{habitId}") {
        fun createRoute(habitId: Long) = "stats_habit_heatmap/$habitId"
    }
    object Settings : Screen("settings")
}

@Composable
fun HabsNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Today.route) {
        composable(Screen.Today.route) {
            TodayScreen(
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateFullList = { navController.navigate(Screen.TodayFull.route) }
            )
        }
        composable(Screen.TodayFull.route) {
            val parentEntry = remember {
                navController.getBackStackEntry(Screen.Today.route)
            }
            TodayFullListScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = hiltViewModel(parentEntry)
            )
        }
        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onOpenHabitDetail = { habitId ->
                    navController.navigate(Screen.HabitStats.createRoute(habitId))
                }
            )
        }
        composable(
            route = Screen.HabitStats.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { entry ->
            val habitId = entry.arguments!!.getLong("habitId")
            HabitStatsDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenCalendarHeatmap = {
                    navController.navigate(Screen.HabitStatsHeatmap.createRoute(habitId))
                }
            )
        }
        composable(
            route = Screen.HabitStatsHeatmap.route,
            arguments = listOf(
                navArgument("habitId") { type = NavType.LongType }
            )
        ) { entry ->
            val habitId = entry.arguments!!.getLong("habitId")
            val parentEntry = remember(habitId) {
                navController.getBackStackEntry(Screen.HabitStats.createRoute(habitId))
            }
            HabitStatsHeatmapScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = hiltViewModel(parentEntry)
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}