package com.example.paisapal.ui.screens.detail

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.paisapal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onBackClick: () -> Unit,
    onCategorizeClick: () -> Unit = {},
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val transaction by viewModel.transaction.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showSmsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Transaction Details",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                actions = {
                    // Share button
                    IconButton(onClick = {
                        transaction?.let { tx ->
                            val shareText = buildShareText(tx)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Transaction"))
                        }
                    }) {
                        Icon(Icons.Default.Share, "Share", tint = TextWhite)
                    }

                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = DebitRed)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }
                transaction == null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = DebitRed,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Transaction not found", color = TextWhite)
                    }
                }
                else -> {
                    TransactionDetailContent(
                        transaction = transaction!!,
                        onCategoryClick = { showCategoryDialog = true },
                        onViewSmsClick = { showSmsDialog = true },
                        onEditCategoryClick = onCategorizeClick  // ✅ PASS IT
                    )
                }
            }
        }
    }

    // Dialogs
    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                transaction?.let {
                    viewModel.deleteTransaction(it) {
                        onBackClick()
                    }
                }
                showDeleteDialog = false
            }
        )
    }

    if (showCategoryDialog) {
        EditCategoryDialog(
            currentCategory = transaction?.category ?: "",
            onDismiss = { showCategoryDialog = false },
            onConfirm = { newCategory ->
                viewModel.updateCategory(newCategory)
                showCategoryDialog = false
            }
        )
    }

    if (showSmsDialog) {
        ViewSmsDialog(
            smsBody = transaction?.smsBody ?: "",
            onDismiss = { showSmsDialog = false }
        )
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    onCategoryClick: () -> Unit,
    onViewSmsClick: () -> Unit,
    onEditCategoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Amount Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (transaction.type == TransactionType.DEBIT)
                    DebitRed.copy(alpha = 0.1f)
                else
                    CreditGreen.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (transaction.type == TransactionType.DEBIT) "Debited" else "Credited",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "₹${String.format("%.2f", transaction.amount)}",
                    color = if (transaction.type == TransactionType.DEBIT) DebitRed else CreditGreen,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // ✅ ACCOUNT INFORMATION CARD
        if (transaction.accountLast4Digits != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AccountBox,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "Account",
                                color = TextGray,
                                fontSize = 12.sp
                            )
                            Text(
                                transaction.accountName ?: "Account",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryBlue.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "****${transaction.accountLast4Digits}",
                            color = PrimaryBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Merchant Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    label = "Merchant",
                    value = transaction.merchantDisplayName ?: transaction.merchantRaw ?: "Unknown"
                )

                if (transaction.merchantRaw != null &&
                    transaction.merchantDisplayName != transaction.merchantRaw) {
                    DetailRow(
                        label = "Raw Name",
                        value = transaction.merchantRaw ?: "",
                        valueColor = TextGray
                    )
                }
            }
        }

        // Category Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onCategoryClick),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Category", color = TextGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        transaction.category ?: "Uncategorized",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (transaction.needsReview) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Needs Review",
                            color = Color(0xFFFF9800),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onEditCategoryClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Category",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Transaction Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    label = "Date & Time",
                    value = formatTimestamp(transaction.timestamp)
                )

                DetailRow(
                    label = "Type",
                    value = transaction.type.name,
                    valueColor = if (transaction.type == TransactionType.DEBIT) DebitRed else CreditGreen
                )

                transaction.referenceNumber?.let {
                    DetailRow(
                        label = "Reference Number",
                        value = it
                    )
                }

                transaction.upiVpa?.let {
                    DetailRow(
                        label = "UPI ID",
                        value = it
                    )
                }

                DetailRow(
                    label = "Sender",
                    value = transaction.sender
                )

                transaction.contextConfidence?.let {
                    DetailRow(
                        label = "Confidence",
                        value = "${(it * 100).toInt()}%",
                        valueColor = if (it > 0.7f) CreditGreen else Color(0xFFFF9800)
                    )
                }
            }
        }

        // View SMS Button
        OutlinedButton(
            onClick = onViewSmsClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PrimaryBlue
            ),
            border = BorderStroke(1.dp, PrimaryBlue)
        ) {
            Icon(
                Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("View Original SMS")
        }
    }
}
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
private fun AmountCard(transaction: Transaction) {
    val isCredit = transaction.type == TransactionType.CREDIT

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCredit) "Income" else "Expense",
                color = TextGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${if (isCredit) "+" else "-"}₹${String.format("%.2f", transaction.amount)}",
                color = if (isCredit) CreditGreen else DebitRed,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDate(transaction.timestamp),
                color = TextGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DetailsCard(
    transaction: Transaction,
    onCategoryClick: () -> Unit,
    onEditCategoryClick: () -> Unit  // ✅ ADD THIS
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Details",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // ✅ Button to categorize screen
                TextButton(onClick = onEditCategoryClick) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Edit", color = PrimaryGreen, fontSize = 14.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow("Merchant", transaction.merchantDisplayName ?: "Unknown")
            DetailRow("Category", transaction.category ?: "Uncategorized")
            transaction.upiVpa?.let {
                DetailRow("UPI ID", it)
            }
            transaction.referenceNumber?.let {
                DetailRow("Reference", it)
            }
        }
    }
}

@Composable
private fun SmsCard(transaction: Transaction, onViewSmsClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SMS Details",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onViewSmsClick) {
                    Text("View Full SMS", color = PrimaryGreen)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = transaction.smsBody.take(100) + if (transaction.smsBody.length > 100) "..." else "",
                color = TextGray,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun MetadataCard(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Metadata",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow("Sender", transaction.sender)
            DetailRow("Transaction ID", transaction.id.take(8) + "...")
            DetailRow("Status", if (transaction.needsReview) "Needs Review" else "Categorized")
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = TextWhite,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                value,
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            action?.invoke()
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Delete Transaction?", color = TextWhite) },
        text = { Text("This action cannot be undone.", color = TextGray) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = DebitRed)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun EditCategoryDialog(
    currentCategory: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(currentCategory) }
    val categories = listOf(
        "Food & Dining", "Shopping", "Transportation", "Groceries",
        "Entertainment", "Utilities", "Health & Fitness", "Education", "Transfer", "Other"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Change Category", color = TextWhite) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(category, color = TextWhite)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedCategory) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun ViewSmsDialog(
    smsBody: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Full SMS", color = TextWhite) },
        text = {
            Text(
                text = smsBody,
                color = TextWhite,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryGreen)
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun buildShareText(transaction: Transaction): String {
    return """
        Transaction Details
        
        Amount: ${if (transaction.type == TransactionType.CREDIT) "+" else "-"}₹${transaction.amount}
        Merchant: ${transaction.merchantDisplayName ?: "Unknown"}
        Category: ${transaction.category ?: "Uncategorized"}
        Date: ${formatDate(transaction.timestamp)}
        ${transaction.upiVpa?.let { "UPI: $it" } ?: ""}
        ${transaction.referenceNumber?.let { "Ref: $it" } ?: ""}
        
        Sent via PaisaPal
    """.trimIndent()
}