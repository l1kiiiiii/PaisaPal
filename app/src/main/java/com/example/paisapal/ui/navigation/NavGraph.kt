package com.example.paisapal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.paisapal.ui.screens.categorize.CategorizeScreen
import com.example.paisapal.ui.screens.detail.TransactionDetailScreen
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.imports.ImportSmsScreen
import com.example.paisapal.ui.screens.insights.InsightsScreen
import com.example.paisapal.ui.screens.review.ReviewScreen
import com.example.paisapal.ui.screens.settings.SettingsScreen

@Composable
fun PaisaPalNavGraph(
    navController: NavHostController,
    onCurrentRouteChange: (String) -> Unit = {}
) {
    NavHost(navController = navController, startDestination = "home") {
        // Main Tab Screens
        composable("home") {
            HomeScreen(
                onImportClick = { navController.navigate("import_sms") },
                onTransactionClick = { transaction ->
                    navController.navigate("detail/${transaction.id}")
                }
            )
            onCurrentRouteChange("home")
        }

        composable("review") {
            ReviewScreen(
                onCategorizeClick = { transaction ->
                    navController.navigate("categorize/${transaction.id}")
                }
            )
            onCurrentRouteChange("review")
        }

        composable("insights") {
            InsightsScreen()
            onCurrentRouteChange("insights")
        }

        composable("settings") {
            SettingsScreen(onBackClick = { navController.popBackStack() })
            onCurrentRouteChange("settings")
        }

        // Nested Screens (No Bottom Nav)
        composable("import_sms") {
            ImportSmsScreen(onBackClick = { navController.popBackStack() })
            onCurrentRouteChange("import_sms")
        }

        // FIXED: Pass ID, fetch in ViewModel
        composable(
            route = "detail/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            if (transactionId != null) {
                TransactionDetailScreen(
                    transactionId = transactionId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            onCurrentRouteChange("detail")
        }

        // FIXED: Pass ID, fetch in ViewModel
        composable(
            route = "categorize/{transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            if (transactionId != null) {
                CategorizeScreen(
                    transactionId = transactionId,
                    onBackClick = { navController.popBackStack() },
                    onCategorySelected = { navController.popBackStack() }
                )
            }
            onCurrentRouteChange("categorize")
        }
    }
}
