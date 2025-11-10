package com.example.paisapal.ui.screens.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.components.CompactTopBar
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budgetSummary by viewModel.budgetSummary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CompactTopBar("Budgets")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryGreenLight
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget", tint = TextWhite)
            }
        }
    ) { paddingValues ->

        if (isLoading || budgetSummary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreenLight)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Budget Card
                item {
                    OverallBudgetCard(budgetSummary!!)
                }

                // Alerts
                if (budgetSummary!!.overBudgetCount > 0 || budgetSummary!!.nearLimitCount > 0) {
                    item {
                        AlertsCard(budgetSummary!!)
                    }
                }

                // Category Budgets Header
                item {
                    Text(
                        "Category Budgets",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                // Category Budget List
                items(budgetSummary!!.categoryBudgets) { budget ->
                    BudgetCard(budget, onEdit = { /* TODO */ })
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amount ->
                viewModel.createBudget(category, amount)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun OverallBudgetCard(summary: com.example.domain.model.BudgetSummary) {
    val progress by animateFloatAsState(
        targetValue = summary.usagePercentage / 100f,
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Overall Budget",
                fontSize = 16.sp,
                color = TextGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "₹${String.format("%.0f", summary.totalSpent)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.totalSpent > summary.totalBudget) DebitRed else TextWhite
                    )
                    Text(
                        "of ₹${String.format("%.0f", summary.totalBudget)}",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }

                Text(
                    "${String.format("%.0f", summary.usagePercentage)}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        summary.usagePercentage >= 100 -> DebitRed
                        summary.usagePercentage >= 80 -> WarningOrange
                        else -> CreditGreen
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = when {
                    summary.usagePercentage >= 100 -> DebitRed
                    summary.usagePercentage >= 80 -> WarningOrange
                    else -> PrimaryGreenLight
                },
                trackColor = DividerColor,
            )
        }
    }
}

@Composable
private fun AlertsCard(summary: com.example.domain.model.BudgetSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (summary.overBudgetCount > 0) DebitRed.copy(alpha = 0.2f) else WarningOrange.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = if (summary.overBudgetCount > 0) DebitRed else WarningOrange
            )

            Column {
                if (summary.overBudgetCount > 0) {
                    Text(
                        "${summary.overBudgetCount} budget(s) exceeded",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                if (summary.nearLimitCount > 0) {
                    Text(
                        "${summary.nearLimitCount} budget(s) near limit",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetCard(
    budget: com.example.domain.model.Budget,
    onEdit: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = (budget.usagePercentage / 100f).coerceIn(0f, 1f),
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    budget.category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )

                Text(
                    "${String.format("%.0f", budget.usagePercentage)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        budget.isOverBudget -> DebitRed
                        budget.isNearLimit -> WarningOrange
                        else -> CreditGreen
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "₹${String.format("%.0f", budget.spentAmount)} / ₹${String.format("%.0f", budget.limitAmount)}",
                    fontSize = 12.sp,
                    color = TextGray
                )

                Text(
                    "₹${String.format("%.0f", budget.remainingAmount)} left",
                    fontSize = 12.sp,
                    color = if (budget.isOverBudget) DebitRed else CreditGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    budget.isOverBudget -> DebitRed
                    budget.isNearLimit -> WarningOrange
                    else -> PrimaryGreenLight
                },
                trackColor = DividerColor,
            )
        }
    }
}

@Composable
private fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Budget Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (category.isNotBlank() && amt != null) {
                        onConfirm(category, amt)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
