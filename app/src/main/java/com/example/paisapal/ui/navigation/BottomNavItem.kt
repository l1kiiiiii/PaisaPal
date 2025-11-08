package com.example.paisapal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Review : BottomNavItem("review", "Review", Icons.Default.Search)
    object Insights : BottomNavItem("insights", "Insights", Icons.Default.TrendingUp)
    object Settings : BottomNavItem("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Review,
    BottomNavItem.Insights,
    BottomNavItem.Settings
)
