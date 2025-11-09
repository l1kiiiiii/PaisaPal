package com.example.paisapal

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.paisapal.ui.navigation.bottomNavItems
import com.example.paisapal.ui.screens.budget.BudgetScreen
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.imports.ImportSmsScreen
import com.example.paisapal.ui.screens.insights.InsightsScreen
import com.example.paisapal.ui.screens.review.ReviewScreen
import com.example.paisapal.ui.screens.settings.SettingsScreen
import com.example.paisapal.ui.theme.PrimaryBlue

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf("home") }

    // Routes that should NOT show bottom navigation
    val navWithoutBottomBar = listOf(
        "import_sms",
        "detail",
        "categorize"
    )

    val showBottomBar = !navWithoutBottomBar.any { currentRoute.startsWith(it) }

    Scaffold(
        containerColor = Color.Black,  // Pitch black background
        bottomBar = {
            if (showBottomBar) {
                PaisaPalBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        currentRoute = route
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        // NavHost with padding applied
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)  // Fixed padding issue
        ) {
            composable("home") {
                currentRoute = "home"
                HomeScreen()
            }
            composable("review") {
                currentRoute = "review"
                ReviewScreen()
            }
            composable("budget") {
                currentRoute = "budget"
                BudgetScreen()
            }
            composable("insights") {
                currentRoute = "insights"
                InsightsScreen()
            }
            composable("settings") {
                currentRoute = "settings"
                SettingsScreen()
            }
            composable("import_sms") {
                currentRoute = "import_sms"
                ImportSmsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun PaisaPalBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.Transparent,  // Transparent background
        contentColor = Color.White
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) PrimaryBlue else Color.Gray  // Blue when selected
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) PrimaryBlue else Color.Gray  // Blue text
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,       // Blue icon
                    selectedTextColor = PrimaryBlue,       // Blue text
                    unselectedIconColor = Color.Gray,      // Gray unselected
                    unselectedTextColor = Color.Gray,      // Gray unselected
                    indicatorColor = Color.Transparent     // No background indicator
                )
            )
        }
    }
}
