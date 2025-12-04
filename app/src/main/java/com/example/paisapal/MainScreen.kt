package com.example.paisapal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.paisapal.ui.navigation.bottomNavItems
import com.example.paisapal.ui.screens.budget.BudgetScreen
import com.example.paisapal.ui.screens.categorize.CategorizeScreen
import com.example.paisapal.ui.screens.detail.TransactionDetailScreen
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.insights.InsightsScreen
import com.example.paisapal.ui.screens.notification.NotificationDebugScreen
import com.example.paisapal.ui.screens.review.ReviewScreen
import com.example.paisapal.ui.screens.settings.SettingsScreen
import com.example.paisapal.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes without bottom bar
    val navWithoutBottomBar = listOf(
        "import_sms",
        "transaction_detail/{transactionId}",
        "categorize/{transactionId}",
        "notification_debug",
        "add_manual_transaction"
    )

    val showBottomBar = navWithoutBottomBar.none { route ->
        currentRoute?.startsWith(route.substringBefore("{")) == true
    }

    BackHandler(enabled = currentRoute != "home") {
        if (currentRoute in listOf("review", "budget", "insights", "settings", "notification_debug", "add_manual_transaction")) {
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        } else {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            if (currentRoute == "home") {
                // ✅ TopAppBar with Notification Icon (Right)
                TopAppBar(
                    title = {
                        Text(
                            text = "PaisaPal",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("notification_debug") }) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "View Notifications",
                                tint = PrimaryBlue
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1A1A1A)
                    )
                )
            }
        },
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
        },
        floatingActionButton = {
            if (currentRoute == "home") {
                // ✅ FAB for Manual Add Transaction (Blue)
                FloatingActionButton(
                    onClick = { navController.navigate("add_manual_transaction") },
                    containerColor = PrimaryBlue
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Manual Transaction",
                        tint = Color.White
                    )
                }
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

            composable("notification_debug") {
                NotificationDebugScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ✅ Add Manual Transaction Screen Route
            composable("add_manual_transaction") {
                // TODO: Create ManualTransactionScreen composable
                // For now, use a placeholder screen
                ManualTransactionPlaceholder(
                    onSave = {
                        // TODO: Save transaction via ViewModel/repository
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack() }
                )
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

// ✅ TEMPORARY PLACEHOLDER - Replace with real ManualTransactionScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualTransactionPlaceholder(
    onSave: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Manual Transaction Screen", color = Color.White, fontSize = 24.sp)
            Text("TODO: Implement form fields", color = Color.Gray)
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Save (Placeholder)", color = Color.White)
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
