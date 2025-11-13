package com.example.paisapal.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.components.CompactTopBar
import com.example.paisapal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val reviewItems by viewModel.reviewItems.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar when message is set
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CompactTopBar("Review Transactions")
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        if (reviewItems.isEmpty()) {
            EmptyReviewState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "${reviewItems.size} transactions need review",
                        color = TextGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(reviewItems) { item ->
                    SmartSuggestionCard(
                        item = item,
                        onConfirm = { category ->
                            viewModel.confirmCategory(
                                transactionId = item.transaction.id,
                                category = category,
                                merchantKeyword = item.transaction.merchantRaw?.take(10)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartSuggestionCard(
    item: ReviewItem,
    onConfirm: (String) -> Unit
) {
    var showCategoryDialog by remember { mutableStateOf(false) }

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
            // Transaction Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.transaction.merchantDisplayName ?: "Unknown Merchant",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formatDateTime(item.transaction.timestamp),
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "₹${String.format("%.2f", item.transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = DebitRed,
                    fontSize = 18.sp
                )
            }

            HorizontalDivider(color = SurfaceLighter, thickness = 0.5.dp)

            // Suggestion
            Text(
                text = "💡 We think this is '${item.suggestedCategory}'",
                color = PrimaryBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showCategoryDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextWhite
                    )
                ) {
                    Text("Change")
                }

                Button(
                    onClick = { onConfirm(item.suggestedCategory) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    )
                ) {
                    Text("Confirm", color = Color.White)
                }
            }
        }
    }

    // Category Selection Dialog
    if (showCategoryDialog) {
        CategorySelectionDialog(
            onDismiss = { showCategoryDialog = false },
            onCategorySelected = { category ->
                onConfirm(category)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
private fun CategorySelectionDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "Food & Dining",
        "Shopping",
        "Transportation",
        "Entertainment",
        "Bills & Utilities",
        "Healthcare",
        "Education",
        "Others"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Select Category", color = TextWhite) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { category ->
                    TextButton(
                        onClick = { onCategorySelected(category) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            category,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                            color = TextWhite
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        }
    )
}

@Composable
private fun EmptyReviewState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✅", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "All Caught Up!",
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "All your transactions are categorized",
            color = TextGray,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        .format(Date(timestamp))
}
