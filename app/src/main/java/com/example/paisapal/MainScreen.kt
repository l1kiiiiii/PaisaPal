package com.example.paisapal

import androidx.compose.foundation.background
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.paisapal.ui.navigation.BottomNavItem
import com.example.paisapal.ui.navigation.PaisaPalNavGraph
import com.example.paisapal.ui.navigation.bottomNavItems
import com.example.paisapal.ui.theme.*

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
        bottomBar = {
            if (showBottomBar) {
                PaisaPalBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
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
            PaisaPalNavGraph(
            navController = navController,
            onCurrentRouteChange = { newRoute ->
                currentRoute = newRoute
            }
        )
    }
}

@Composable
private fun PaisaPalBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        modifier = Modifier.background(PrimaryGreen),
        containerColor = PrimaryGreen,
        contentColor = TextWhite
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TextWhite,
                    selectedTextColor = TextWhite,
                    indicatorColor = PrimaryGreenLight,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray
                )
            )
        }
    }
}
