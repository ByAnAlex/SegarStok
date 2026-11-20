package com.segarstok.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.segarstok.feature.addbatch.AddBatchScreen
import com.segarstok.feature.batchdetail.BatchDetailScreen
import com.segarstok.feature.dashboard.DashboardScreen
import com.segarstok.feature.recommendation.RecommendationScreen
import com.segarstok.feature.settings.SettingsScreen
import com.segarstok.feature.stocklist.StockListScreen

@Composable
fun SegarStokAppMain(
    navController: NavHostController = rememberNavController()
) {
    val bottomNavItems = listOf(
        ScreenRoute.Dashboard,
        ScreenRoute.StockList,
        ScreenRoute.Recommendation,
        ScreenRoute.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Text(getTabIconEmoji(screen)) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ScreenRoute.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAddBatch = { navController.navigate(ScreenRoute.AddBatch.route) },
                    onNavigateToBatchDetail = { id -> navController.navigate(ScreenRoute.BatchDetail.createRoute(id)) },
                    onNavigateToRecommendations = { navController.navigate(ScreenRoute.Recommendation.route) }
                )
            }

            composable(ScreenRoute.StockList.route) {
                StockListScreen(
                    onNavigateToAddBatch = { navController.navigate(ScreenRoute.AddBatch.route) },
                    onNavigateToBatchDetail = { id -> navController.navigate(ScreenRoute.BatchDetail.createRoute(id)) }
                )
            }

            composable(ScreenRoute.AddBatch.route) {
                AddBatchScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = ScreenRoute.BatchDetail.route,
                arguments = listOf(navArgument("batchId") { type = NavType.LongType })
            ) { backStackEntry ->
                val batchId = backStackEntry.arguments?.getLong("batchId") ?: 0L
                BatchDetailScreen(
                    batchId = batchId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoute.Recommendation.route) {
                RecommendationScreen(
                    onNavigateToBatchDetail = { id -> navController.navigate(ScreenRoute.BatchDetail.createRoute(id)) }
                )
            }

            composable(ScreenRoute.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

private fun getTabIconEmoji(screen: ScreenRoute): String {
    return when (screen) {
        ScreenRoute.Dashboard -> "🏠"
        ScreenRoute.StockList -> "📦"
        ScreenRoute.Recommendation -> "💡"
        ScreenRoute.Settings -> "⚙️"
        else -> "📄"
    }
}
