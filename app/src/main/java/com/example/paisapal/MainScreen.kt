package com.example.paisapal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paisapal.ui.components.PaisaPalBottomBar
import com.example.paisapal.ui.screens.accounts.ManageAccountsScreen
import com.example.paisapal.ui.screens.budget.BudgetScreen
import com.example.paisapal.ui.screens.categorize.CategorizeScreen
import com.example.paisapal.ui.screens.detail.TransactionDetailScreen
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.insights.InsightsScreen
import com.example.paisapal.ui.screens.notification.NotificationDebugScreen
import com.example.paisapal.ui.screens.review.ReviewScreen
import com.example.paisapal.ui.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showQuickAddDialog by remember { mutableStateOf(false) }

    // Routes that should NOT show the bottom bar (ALL preserved)
    val navWithoutBottomBar = listOf(
        "import_sms",
        "transaction_detail/{transactionId}",
        "categorize/{transactionId}",
        "notification_debug",
        "manage_accounts"
    )

    val showBottomBar = navWithoutBottomBar.none { route ->
        currentRoute?.startsWith(route.substringBefore("{")) == true
    }

    BackHandler(enabled = currentRoute != "home") {
        if (currentRoute in listOf("review", "budget", "settings", "notification_debug")) {
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
                // ✅ NEW: Floating dock with integrated FAB
                PaisaPalBottomBar(
                    currentRoute = currentRoute ?: "home",
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onFabClick = { showQuickAddDialog = true } // ✅ Triggers HomeScreen dialog
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            // ===== MAIN TABS (3 tabs only) =====

            composable("home") {
                HomeScreen(
                    onTransactionClick = { transaction ->
                        navController.navigate("transaction_detail/${transaction.id}")
                    },
                    onReviewClick = {
                        navController.navigate("review")
                    },
                    showQuickAddDialog = showQuickAddDialog,              // ✅ PASSED
                    onDismissQuickAdd = { showQuickAddDialog = false }    // ✅ PASSED
                )
            }

            // ✅ MERGED Budget + Insights tab (shows both)
            composable("budget") {
                BudgetAndInsightsScreen(
                    onBackClick = { /* Already handled by BackHandler */ }
                )
            }

            composable("settings") {
                SettingsScreen(
                    onNavigateToAccounts = {
                        navController.navigate("manage_accounts")
                    }
                )
            }

            // ===== OTHER SCREENS (ALL preserved) =====

            composable("review") {
                ReviewScreen()
            }

            composable("manage_accounts") {
                ManageAccountsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("notification_debug") {
                NotificationDebugScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ===== DETAIL SCREENS (ALL preserved) =====

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
                        onBackClick = { navController.popBackStack() },
                        onCategorizeClick = { navController.navigate("categorize/$transactionId") }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            }

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
                        onBackClick = { navController.popBackStack() },
                        onCategorizeComplete = { category ->
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

// ✅ NEW: Merged Budget + Insights screen
@Composable
private fun BudgetAndInsightsScreen(onBackClick: () -> Unit) {
    // Simple tab switcher - replace with TabRow or HorizontalPager later
    var selectedTab by remember { mutableStateOf("budget") }

    Column {
        // Tab Headers
        TabRow(
            selectedTabIndex = if (selectedTab == "budget") 0 else 1
        ) {
            Tab(
                selected = selectedTab == "budget",
                onClick = { selectedTab = "budget" },
                text = { Text("Budget") }
            )
            Tab(
                selected = selectedTab == "insights",
                onClick = { selectedTab = "insights" },
                text = { Text("Insights") }
            )
        }

        // Tab Content
        when (selectedTab) {
            "budget" -> BudgetScreen()
            "insights" -> InsightsScreen()
        }
    }
}
