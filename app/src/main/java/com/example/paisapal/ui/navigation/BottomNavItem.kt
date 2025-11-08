package com.example.paisapal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Review : BottomNavItem("review", "Review", Icons.Default.Search)
    object Budget : BottomNavItem("budget", "Budget", Icons.Default.AccountBalance)
    object Insights : BottomNavItem("insights", "Insights", Icons.AutoMirrored.Filled.TrendingUp)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Review,
    BottomNavItem.Budget,
    BottomNavItem.Insights,
    BottomNavItem.Settings
)
