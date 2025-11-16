package com.example.paisapal.ui.screens.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.usecase.BudgetSummary
import com.example.paisapal.ui.components.CompactTopBar
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val budgetSummaries by viewModel.budgetSummaries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
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

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreenLight)
            }
        } else if (budgetSummaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "No budgets yet",
                        fontSize = 18.sp,
                        color = TextGray
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreenLight
                        )
                    ) {
                        Text("Create Your First Budget")
                    }
                }
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
                item {
                    OverallBudgetCard(budgetSummaries)
                }

                val overBudgetCount = budgetSummaries.count { it.isOverBudget }
                val nearLimitCount = budgetSummaries.count { it.progress >= 0.8f && !it.isOverBudget }

                if (overBudgetCount > 0 || nearLimitCount > 0) {
                    item {
                        AlertsCard(overBudgetCount, nearLimitCount)
                    }
                }

                item {
                    Text(
                        "Category Budgets",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                items(budgetSummaries) { budget ->
                    BudgetCard(
                        budget = budget,
                        onDelete = { viewModel.deleteBudgetByCategory(budget.category) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddBudgetDialog(
            availableCategories = availableCategories,
            onDismiss = { showAddDialog = false },
            onConfirm = { category, amount ->
                viewModel.createBudget(category, amount)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun OverallBudgetCard(summaries: List<BudgetSummary>) {
    val totalBudget = summaries.sumOf { it.budgetAmount }
    val totalSpent = summaries.sumOf { it.spentAmount }
    val overallProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f

    val progress by animateFloatAsState(
        targetValue = overallProgress,
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
                        "₹${String.format("%.0f", totalSpent)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalSpent > totalBudget) DebitRed else TextWhite
                    )
                    Text(
                        "of ₹${String.format("%.0f", totalBudget)}",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }

                Text(
                    "${String.format("%.0f", overallProgress * 100)}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        overallProgress >= 1.0f -> DebitRed
                        overallProgress >= 0.8f -> WarningOrange
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
                    overallProgress >= 1.0f -> DebitRed
                    overallProgress >= 0.8f -> WarningOrange
                    else -> PrimaryGreenLight
                },
                trackColor = DividerColor,
            )
        }
    }
}

@Composable
private fun AlertsCard(overBudgetCount: Int, nearLimitCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (overBudgetCount > 0) DebitRed.copy(alpha = 0.2f) else WarningOrange.copy(alpha = 0.2f)
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
                tint = if (overBudgetCount > 0) DebitRed else WarningOrange
            )

            Column {
                if (overBudgetCount > 0) {
                    Text(
                        "$overBudgetCount budget(s) exceeded",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                if (nearLimitCount > 0) {
                    Text(
                        "$nearLimitCount budget(s) near limit",
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
    budget: BudgetSummary,
    onDelete: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = budget.progress,
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${String.format("%.0f", budget.progress * 100)}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            budget.isOverBudget -> DebitRed
                            budget.progress >= 0.8f -> WarningOrange
                            else -> CreditGreen
                        }
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Budget",
                            tint = TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "₹${String.format("%.0f", budget.spentAmount)} / ₹${String.format("%.0f", budget.budgetAmount)}",
                    fontSize = 12.sp,
                    color = TextGray
                )

                Text(
                    "₹${String.format("%.0f", budget.remainingAmount)} ${if (budget.isOverBudget) "over" else "left"}",
                    fontSize = 12.sp,
                    color = if (budget.isOverBudget) DebitRed else CreditGreen
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    budget.isOverBudget -> DebitRed
                    budget.progress >= 0.8f -> WarningOrange
                    else -> PrimaryGreenLight
                },
                trackColor = DividerColor,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetDialog(
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget", color = TextWhite) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreenLight,
                            unfocusedBorderColor = DividerColor
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = it
                        }
                    },
                    label = { Text("Budget Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreenLight,
                        unfocusedBorderColor = DividerColor
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (selectedCategory.isNotBlank() && amt != null && amt > 0) {
                        onConfirm(selectedCategory, amt)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreenLight
                ),
                enabled = selectedCategory.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        },
        containerColor = SurfaceDark,
        titleContentColor = TextWhite,
        textContentColor = TextWhite
    )
}
