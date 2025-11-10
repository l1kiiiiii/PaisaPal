package com.example.paisapal.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.paisapal.ui.screens.budget.BudgetScreen
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.insights.InsightsScreen
import com.example.paisapal.ui.screens.review.ReviewScreen
import com.example.paisapal.ui.screens.settings.SettingsScreen
import com.example.paisapal.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf("home") }

    Scaffold(
        containerColor = Color.Black,  //  Pitch black background
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,  //  Transparent background
                contentColor = Color.White
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) PrimaryBlue else Color.Gray  //  Blue when selected
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                color = if (isSelected) PrimaryBlue else Color.Gray  //  Blue when selected
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                currentRoute = item.route
                                navController.navigate(item.route) {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryBlue,           //  Blue icon
                            selectedTextColor = PrimaryBlue,           //  Blue text
                            unselectedIconColor = Color.Gray,          // Gray unselected
                            unselectedTextColor = Color.Gray,          // Gray unselected
                            indicatorColor = Color.Transparent         //  No background indicator
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
        }
    }
}
