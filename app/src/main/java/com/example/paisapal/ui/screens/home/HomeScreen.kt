package com.example.paisapal.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.paisapal.ui.components.CompactTopBar
import com.example.paisapal.ui.theme.BackgroundDark
import com.example.paisapal.ui.theme.CreditGreen
import com.example.paisapal.ui.theme.DebitRed
import com.example.paisapal.ui.theme.PrimaryBlue
import com.example.paisapal.ui.theme.PrimaryGreen
import com.example.paisapal.ui.theme.SurfaceDark
import com.example.paisapal.ui.theme.SurfaceLighter
import com.example.paisapal.ui.theme.TextGray
import com.example.paisapal.ui.theme.TextWhite
import com.example.paisapal.ui.theme.WarningOrange
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onTransactionClick: (Transaction) -> Unit = {},
    onReviewClick: () -> Unit = {}
) {
    val smartFeedItems by viewModel.smartFeedItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // ADD: Dialog state
    var showQuickAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CompactTopBar("Home", onSettingsClick = {}) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddDialog = true },
                containerColor = PrimaryGreen
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Quick Add",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryGreen)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(smartFeedItems) { item ->
                        when (item) {
                            is SmartFeedItem.OverviewCard -> {
                                OverviewCard(
                                    totalSpent = item.totalSpent,
                                    budgetStatus = item.budgetStatus,
                                    budgetProgress = item.budgetProgress
                                )
                            }
                            is SmartFeedItem.NeedsReviewBanner -> {
                                NeedsReviewBanner(
                                    count = item.count,
                                    onClick = onReviewClick
                                )
                            }
                            is SmartFeedItem.TransactionSection -> {
                                TransactionListSection(
                                    transactions = item.transactions,
                                    onTransactionClick = onTransactionClick
                                )
                            }
                            is SmartFeedItem.BudgetAlert -> {
                                BudgetAlertCard(
                                    category = item.category,
                                    overage = item.overage
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD: Quick Add Dialog
    if (showQuickAddDialog) {
        QuickAddDialog(
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { transaction ->
                viewModel.addManualTransaction(transaction)
                showQuickAddDialog = false
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var merchantName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.DEBIT) }
    var selectedCategory by remember { mutableStateOf("Shopping") }
    var expanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Food & Dining",
        "Shopping",
        "Transportation",
        "Groceries",
        "Entertainment",
        "Utilities",
        "Health & Fitness",
        "Education",
        "Transfer",
        "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(
                text = "Quick Add Transaction",
                color = TextWhite,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Transaction Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.DEBIT,
                        onClick = { selectedType = TransactionType.DEBIT },
                        label = { Text("Expense") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DebitRed,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.CREDIT,
                        onClick = { selectedType = TransactionType.CREDIT },
                        label = { Text("Income") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CreditGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount", color = TextGray) },
                    placeholder = { Text("0.00", color = TextGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SurfaceLighter,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                // Merchant Name Input
                OutlinedTextField(
                    value = merchantName,
                    onValueChange = { merchantName = it },
                    label = { Text("Merchant Name", color = TextGray) },
                    placeholder = { Text("e.g., Zomato", color = TextGray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = SurfaceLighter,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    )
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category", color = TextGray) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = SurfaceLighter,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceDark)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = TextWhite) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && merchantName.isNotBlank()) {
                        val transaction = Transaction(
                            id = UUID.randomUUID().toString(),
                            amount = amountValue,
                            type = selectedType,
                            timestamp = System.currentTimeMillis(),
                            merchantRaw = merchantName,
                            merchantDisplayName = merchantName,
                            upiVpa = null,
                            referenceNumber = null,
                            category = selectedCategory,
                            sender = "Manual Entry",
                            smsBody = "Manually added transaction",
                            needsReview = false
                        )
                        onConfirm(transaction)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                enabled = amount.toDoubleOrNull() != null && merchantName.isNotBlank()
            ) {
                Text("Add Transaction")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

// ===== Composable Functions =====

@Composable
private fun OverviewCard(
    totalSpent: Double,
    budgetStatus: String,
    budgetProgress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "This Month",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Spent:", color = TextGray, fontSize = 14.sp)
                Text(
                    "₹${String.format("%.2f", totalSpent)}",
                    fontWeight = FontWeight.Bold,
                    color = DebitRed,
                    fontSize = 20.sp
                )
            }

            if (budgetProgress > 0) {
                HorizontalDivider(thickness = 1.dp, color = SurfaceLighter)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$budgetStatus:", color = TextGray, fontSize = 14.sp)
                        Text(
                            "${(budgetProgress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = when {
                                budgetProgress > 1.0f -> DebitRed
                                budgetProgress > 0.8f -> WarningOrange
                                else -> PrimaryGreen
                            },
                            fontSize = 14.sp
                        )
                    }

                    LinearProgressIndicator(
                        progress = { budgetProgress.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = when {
                            budgetProgress > 1.0f -> DebitRed
                            budgetProgress > 0.8f -> WarningOrange
                            else -> PrimaryGreen
                        },
                        trackColor = SurfaceLighter,
                    )
                }
            }
        }
    }
}

@Composable
private fun NeedsReviewBanner(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarningOrange.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = WarningOrange.copy(alpha = 0.3f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = "⚠️", fontSize = 20.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Needs Review",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp
                )
                Text(
                    "$count ${if (count == 1) "transaction" else "transactions"} need categorization",
                    color = TextGray,
                    fontSize = 13.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Review",
                tint = WarningOrange
            )
        }
    }
}

@Composable
private fun TransactionListSection(
    transactions: List<Transaction>,
    onTransactionClick: (Transaction) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Text(
                text = "${transactions.size} items",
                style = MaterialTheme.typography.bodySmall,
                color = TextGray
            )
        }

        transactions.forEach { transaction ->
            TransactionListItem(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) }
            )
        }
    }
}

@Composable
private fun BudgetAlertCard(category: String, overage: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DebitRed.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = DebitRed)
            Column {
                Text(
                    text = "Budget Alert: $category",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite
                )
                Text(
                    text = "Over by ₹${String.format("%.2f", overage)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: Transaction,
    onClick: () -> Unit = {}
) {
    val isCredit = transaction.type == TransactionType.CREDIT

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.merchantDisplayName ?: "Unknown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )

                transaction.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "${if (isCredit) "+" else "-"}₹${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) CreditGreen else DebitRed,
                fontSize = 16.sp
            )
        }
    }
}
