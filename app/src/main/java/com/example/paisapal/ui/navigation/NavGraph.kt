package com.example.paisapal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
        composable("home") {
            HomeScreen(
                onImportClick = { navController.navigate("import_sms") },
                onTransactionClick = { /* Show details */ }
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

        // Nested Screens (no bottom nav)
        composable("import_sms") {
            ImportSmsScreen(onBackClick = { navController.popBackStack() })
            onCurrentRouteChange("import_sms")
        }

        composable("detail/{transactionId}") {
            TransactionDetailScreen(
                transaction = null!!, // Get from ViewModel
                onBackClick = { navController.popBackStack() }
            )
            onCurrentRouteChange("detail")
        }

        composable("categorize/{transactionId}") {
            CategorizeScreen(
                transaction = null!!, // Get from ViewModel
                onCategorySelected = { navController.popBackStack() }
            )
            onCurrentRouteChange("categorize")
        }
    }
}
