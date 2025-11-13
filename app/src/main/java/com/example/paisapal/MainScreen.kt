package com.example.paisapal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paisapal.ui.navigation.bottomNavItems
import com.example.paisapal.ui.screens.budget.BudgetScreen
import com.example.paisapal.ui.screens.categorize.CategorizeScreen  //  ADD THIS
import com.example.paisapal.ui.screens.detail.TransactionDetailScreen
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.insights.InsightsScreen
import com.example.paisapal.ui.screens.review.ReviewScreen
import com.example.paisapal.ui.screens.settings.SettingsScreen
import com.example.paisapal.ui.theme.PrimaryBlue

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes without bottom bar
    val navWithoutBottomBar = listOf(
        "import_sms",
        "transaction_detail/{transactionId}",
        "categorize/{transactionId}"  //  ADD THIS
    )

    val showBottomBar = navWithoutBottomBar.none { route ->
        currentRoute?.startsWith(route.substringBefore("{")) == true
    }

    BackHandler(enabled = currentRoute != "home") {
        if (currentRoute in listOf("review", "budget", "insights", "settings")) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (showBottomBar) {
                PaisaPalBottomNavigation(
                    currentRoute = currentRoute ?: "home",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            // ===== BOTTOM NAV SCREENS =====

            composable("home") {
                HomeScreen(
                    onTransactionClick = { transaction ->
                        navController.navigate("transaction_detail/${transaction.id}")
                    },
                    onReviewClick = {
                        navController.navigate("review")
                    }
                )
            }

            composable("review") {
                ReviewScreen()
            }

            composable("budget") {
                BudgetScreen()
            }

            composable("insights") {
                InsightsScreen()
            }

            composable("settings") {
                SettingsScreen()
            }

            // ===== DETAIL SCREENS =====

            composable(
                route = "transaction_detail/{transactionId}",
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")

                if (transactionId != null) {
                    TransactionDetailScreen(
                        transactionId = transactionId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        //   Navigate to categorize screen
                        onCategorizeClick = {
                            navController.navigate("categorize/$transactionId")
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            }

            //   Categorize Screen Route
            composable(
                route = "categorize/{transactionId}",
                arguments = listOf(
                    navArgument("transactionId") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")

                if (transactionId != null) {
                    CategorizeScreen(
                        transactionId = transactionId,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onCategorizeComplete = { category ->
                            // Return to detail screen with success
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("categorized", category)
                            navController.popBackStack()
                        }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
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
        containerColor = Color.Transparent,
        contentColor = Color.White
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) PrimaryBlue else Color.Gray
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) PrimaryBlue else Color.Gray
                    )
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryBlue,
                    selectedTextColor = PrimaryBlue,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
