package com.example.paisapal.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImportClick: () -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PaisaPal",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    // Import button
                    IconButton(onClick = onImportClick) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Import SMS",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (transactions.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onImportClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Import More SMS"
                    )
                }
            }
        }
    ) { paddingValues ->

        if (transactions.isEmpty()) {
            EmptyStateWithImport(
                modifier = Modifier.padding(paddingValues),
                onImportClick = onImportClick
            )
        } else {
            TransactionList(
                transactions = transactions,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EmptyStateWithImport(
    modifier: Modifier = Modifier,
    onImportClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📱",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Transactions Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Get started by importing your existing bank SMS or wait for new transactions.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Import SMS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "💡 New transactions will appear automatically when you receive bank SMS",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions (${transactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        items(transactions) { transaction ->
            TransactionCard(transaction = transaction)
        }
    }
}

@Composable
private fun TransactionCard(transaction: Transaction) {
    val isCredit = transaction.type == TransactionType.CREDIT

    val backgroundColor = if (isCredit) CreditGreenLight else DebitRedLight
    val amountColor = if (isCredit) CreditGreenDark else DebitRedDark
    val amountPrefix = if (isCredit) "+ ₹" else "- ₹"
    val typeText = if (isCredit) "CREDIT" else "DEBIT"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Type Badge and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = amountColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = typeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = amountColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "$amountPrefix${String.format("%.2f", transaction.amount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timestamp
            Text(
                text = formatTimestamp(transaction.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Sender
            Text(
                text = "From: ${transaction.sender}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )

            // Reference Number (if available)
            transaction.referenceNumber?.let { ref ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ref: $ref",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // SMS Body (truncated)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.5f)
            ) {
                Text(
                    text = transaction.smsBody.take(120) +
                            if (transaction.smsBody.length > 120) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return dateFormat.format(Date(timestamp))
}
