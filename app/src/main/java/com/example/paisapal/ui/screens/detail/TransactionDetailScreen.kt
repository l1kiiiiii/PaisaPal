package com.example.paisapal.ui.screens.detail

import android.content.Intent
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
            // ✅ CUSTOM TOP BAR WITH ACTIONS
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
                            Icons.Default.Error,
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
                        onViewSmsClick = { showSmsDialog = true }
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
                viewModel.updateCategory(transactionId, newCategory)
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
    onViewSmsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Amount Card
        AmountCard(transaction)

        // Details Card
        DetailsCard(transaction, onCategoryClick)

        // SMS Card
        SmsCard(transaction, onViewSmsClick)

        // Metadata Card
        MetadataCard(transaction)
    }
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
private fun DetailsCard(transaction: Transaction, onCategoryClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Details",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            DetailRow("Merchant", transaction.merchantDisplayName ?: "Unknown")
            DetailRow("Category", transaction.category ?: "Uncategorized") {
                IconButton(onClick = onCategoryClick) {
                    Icon(Icons.Default.Edit, "Edit", tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                }
            }
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
