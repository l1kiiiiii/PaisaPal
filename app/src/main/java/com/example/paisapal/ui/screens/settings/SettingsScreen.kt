package com.example.paisapal.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {}
) {
    var transactionAlerts by remember { mutableStateOf(true) }
    var budgetAlerts by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Data Section
            SettingsSection(title = "Data") {
                SettingsItem(label = "Merchant Mapping", onClick = {})
                SettingsItem(label = "Export Data", onClick = {})
            }

            // Privacy Section
            SettingsSection(title = "Privacy") {
                SettingsItem(label = "Privacy Policy", onClick = {})
                SettingsItem(label = "Terms of Service", onClick = {})
                SettingsItem(label = "Delete Account", onClick = {}, isDestructive = true)
            }

            // Notifications Section
            SettingsSection(title = "Notifications") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Transaction Alerts", color = TextWhite, fontSize = 16.sp)
                        Text("Receive notifications for new transactions", color = TextGray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = transactionAlerts,
                        onCheckedChange = { transactionAlerts = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Budget Alerts", color = TextWhite, fontSize = 16.sp)
                        Text("Get notified about budget limits", color = TextGray, fontSize = 12.sp)
                    }
                    Switch(
                        checked = budgetAlerts,
                        onCheckedChange = { budgetAlerts = it }
                    )
                }
            }

            // General Section
            SettingsSection(title = "General") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("App Version", color = TextWhite)
                    Text("1.2.3", color = TextGray)
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            title,
            fontSize = 14.sp,
            color = TextGray
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
private fun SettingsItem(
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = if (isDestructive) DebitRed else TextWhite,
            fontSize = 16.sp
        )
        Text("→", color = TextGray)
    }
}
