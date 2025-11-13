package com.example.paisapal

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paisapal.ui.navigation.bottomNavItems
import com.example.paisapal.ui.screens.budget.BudgetScreen
import com.example.paisapal.ui.screens.detail.TransactionDetailScreen
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.insights.InsightsScreen
import com.example.paisapal.ui.screens.review.ReviewScreen
import com.example.paisapal.ui.screens.settings.SettingsScreen
import com.example.paisapal.ui.theme.PrimaryBlue

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var currentRoute by remember { mutableStateOf("home") }

    val navWithoutBottomBar = listOf(
        "import_sms",
        "transaction_detail",
        "categorize"
    )

    val showBottomBar = !navWithoutBottomBar.any { currentRoute.startsWith(it) }

    Scaffold(
        containerColor = Color.Black,
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
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                currentRoute = "home"
                HomeScreen(
                    /*
                    onImportClick = {
                        navController.navigate("import_sms")
                    },

                     */
                    onTransactionClick = { transaction ->
                        navController.navigate("transaction_detail/${transaction.id}")
                    },
                    onReviewClick = {
                        navController.navigate("review")
                    }
                )
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

            composable(
                "transaction_detail/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStackEntry ->
                currentRoute = "transaction_detail"
                val transactionId = backStackEntry.arguments?.getString("transactionId")

                if (transactionId != null) {
                    TransactionDetailScreen(
                        transactionId = transactionId,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                } else {
                    // Handle error - invalid transaction ID
                    LaunchedEffect(Unit) {
                        navController.popBackStack()
                    }
                }
            }
            /*
            composable("import_sms") {
                currentRoute = "import_sms"
                ImportSmsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onImportComplete = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

             */
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
