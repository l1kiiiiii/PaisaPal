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
import androidx.compose.material.icons.filled.Edit
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
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    // 1. Collect Global Data
    val globalBudget by viewModel.globalBudget.collectAsState()
    val totalAllocated by viewModel.totalAllocated.collectAsState()

    val budgetSummaries by viewModel.budgetSummaries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Scaffold(
        topBar = { CompactTopBar("Monthly Budget") },
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
        } else {
            // ✅ FIX: Always show the list, do not block with "Empty State"
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 🟢 1. GLOBAL SALARY CARD (Always Visible)
                item {
                    GlobalSalaryCard(
                        salary = globalBudget,
                        allocated = totalAllocated,
                        onSalaryChanged = { viewModel.setGlobalBudget(it) }
                    )
                }

                // 2. OVER-ALLOCATION ALERT
                val unallocated = globalBudget - totalAllocated
                if (unallocated < 0) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DebitRed.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = DebitRed)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Over-allocated by ₹${abs(unallocated.toInt())}",
                                    color = TextWhite,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // 3. CATEGORY HEADER
                item {
                    Text(
                        "Category Allocations",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 4. EMPTY CATEGORY STATE (Inline)
                if (budgetSummaries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No categories yet. Tap + to add.",
                                color = TextGray
                            )
                        }
                    }
                }

                // 5. CATEGORY LIST
                items(budgetSummaries) { budget ->
                    BudgetCard(
                        budget = budget,
                        onDelete = { viewModel.deleteBudgetByCategory(budget.category) }
                    )
                }

                // Spacer for FAB
                item { Spacer(modifier = Modifier.height(80.dp)) }
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
fun GlobalSalaryCard(
    salary: Double,
    allocated: Double,
    onSalaryChanged: (Double) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(if (salary > 0) salary.toString() else "") }
    val unallocated = salary - allocated

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Monthly Income", color = TextGray, fontSize = 14.sp)

                if (!isEditing) {
                    IconButton(onClick = {
                        textValue = if (salary > 0) salary.toString() else ""
                        isEditing = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Income", tint = PrimaryGreenLight, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Input / Display Row
            if (isEditing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) textValue = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter Salary") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreenLight,
                            unfocusedBorderColor = TextGray,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = textValue.toDoubleOrNull() ?: 0.0
                            onSalaryChanged(amount)
                            isEditing = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Text(
                    text = "₹${String.format("%.0f", salary)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = DividerColor)
            Spacer(modifier = Modifier.height(16.dp))

            // Footer Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Allocated", color = TextGray, fontSize = 12.sp)
                    Text(
                        "₹${String.format("%.0f", allocated)}",
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Free to Budget", color = TextGray, fontSize = 12.sp)
                    Text(
                        "₹${String.format("%.0f", unallocated)}",
                        color = if (unallocated < 0) DebitRed else PrimaryGreenLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
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
                    Surface(
                        color = if (budget.isOverBudget) DebitRed.copy(alpha=0.1f) else PrimaryGreenLight.copy(alpha=0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "${String.format("%.0f", budget.progress * 100)}%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (budget.isOverBudget) DebitRed else PrimaryGreenLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
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

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).background(DividerColor, RoundedCornerShape(4.dp)),
                color = if (budget.isOverBudget) DebitRed else PrimaryGreenLight,
            )

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
                            unfocusedBorderColor = DividerColor,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        availableCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = TextWhite) }, // Fix Text Color
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
                    prefix = { Text("₹") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreenLight,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
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
