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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.components.CompactTopBar
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var transactionAlerts by remember { mutableStateOf(true) }
    var budgetAlerts by remember { mutableStateOf(true) }
    val matchingState by viewModel.matchingState.collectAsState()

    // Show toast when matching completes
    LaunchedEffect(matchingState) {
        when (matchingState) {
            is MatchingState.Success -> {
                // Show success message
            }
            is MatchingState.Error -> {
                // Show error message
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { CompactTopBar("Settings", showSettings = false) }
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
                SettingsItem(
                    label = "Match Transactions",
                    onClick = { viewModel.matchTransactions() },
                    isLoading = matchingState is MatchingState.Loading
                )
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
                        Text(
                            "Receive notifications for new transactions",
                            color = TextGray,
                            fontSize = 12.sp
                        )
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
                        Text(
                            "Get notified about budget limits",
                            color = TextGray,
                            fontSize = 12.sp
                        )
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

            // Matching Status (show when loading or completed)
            if (matchingState is MatchingState.Loading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryGreenLight
                        )
                        Text("Matching transactions...", color = TextWhite)
                    }
                }
            }

            if (matchingState is MatchingState.Success) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CreditGreen.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("✓", fontSize = 24.sp, color = CreditGreen)
                        Text("Transactions matched successfully!", color = TextWhite)
                    }
                }
            }

            if (matchingState is MatchingState.Error) {
                val error = (matchingState as MatchingState.Error).message
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DebitRed.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("✗", fontSize = 24.sp, color = DebitRed)
                        Text("Error: $error", color = TextWhite)
                    }
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
    isDestructive: Boolean = false,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = if (isDestructive) DebitRed else TextWhite,
            fontSize = 16.sp
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = PrimaryGreenLight,
                strokeWidth = 2.dp
            )
        } else {
            Text("→", color = TextGray)
        }
    }
}
