package com.example.paisapal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.paisapal.ui.theme.PrimaryBlue

// 1. Define the 3 Tabs (Home, Budget+Insights, Settings)
enum class BottomTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Home("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    Budget("budget", "Analysis", Icons.Filled.PieChart, Icons.Outlined.PieChart), // Merged Tab
    Settings("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun PaisaPalBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    onFabClick: () -> Unit
) {
    // Determine which tab is selected based on the current route
    val selectedTab = BottomTab.values().find { it.route == currentRoute } ?: BottomTab.Home

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp) // Floating Effect
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 🟢 Left: Navigation Pill ---
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            shape = CircleShape,
            color = Color(0xFF1E1E1E), // Dark card background
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomTab.values().forEach { tab ->
                    val isSelected = currentRoute == tab.route // Check explicitly against route
                    val icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon
                    val iconColor = if (isSelected) Color.White else Color.Gray

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onTabSelected(tab.route) }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.title,
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // --- 🟠 Right: Manual Entry FAB ---
        FloatingActionButton(
            onClick = onFabClick,
            shape = RoundedCornerShape(20.dp), // Squircle Shape
            containerColor = PrimaryBlue,
            contentColor = Color.White,
            modifier = Modifier.size(64.dp),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Manual Entry",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}