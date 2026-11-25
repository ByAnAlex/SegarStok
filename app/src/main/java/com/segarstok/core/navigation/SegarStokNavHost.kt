package com.segarstok.core.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.segarstok.core.design.theme.*
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
        containerColor = CanvasBackground,
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = CardSurface,
                    contentColor = Slate900,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .height(64.dp)
                        .border(1.dp, CardBorder)
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = getTabIcon(screen),
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isSelected) PrimaryTeal else Slate500
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) PrimaryTeal else Slate500
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = PrimaryTealLight,
                                selectedIconColor = PrimaryTeal,
                                selectedTextColor = PrimaryTeal,
                                unselectedIconColor = Slate500,
                                unselectedTextColor = Slate500
                            ),
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

private fun getTabIcon(screen: ScreenRoute): ImageVector {
    return when (screen) {
        ScreenRoute.Dashboard -> Icons.Outlined.Home
        ScreenRoute.StockList -> Icons.AutoMirrored.Outlined.List
        ScreenRoute.Recommendation -> Icons.Outlined.Info
        ScreenRoute.Settings -> Icons.Outlined.Settings
        else -> Icons.Outlined.Home
    }
}
