package com.example.paisapal.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.paisapal.ui.components.CompactTopBar
import com.example.paisapal.ui.theme.BackgroundDark
import com.example.paisapal.ui.theme.CreditGreen
import com.example.paisapal.ui.theme.DebitRed
import com.example.paisapal.ui.theme.PrimaryGreenLight
import com.example.paisapal.ui.theme.SurfaceDark
import com.example.paisapal.ui.theme.SurfaceLighter
import com.example.paisapal.ui.theme.TextGray
import com.example.paisapal.ui.theme.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImportClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        containerColor = Color.Black,
            topBar = {
                CompactTopBar("PaisaPal")
            }
    ) { paddingValues ->

        if (transactions.isEmpty()) {
            EmptyStateWithImport(
                modifier = Modifier.padding(paddingValues),
                onImportClick = onImportClick
            )
        } else {
            HomeContent(
                transactions = transactions,
                modifier = Modifier.padding(paddingValues),
                onTransactionClick = onTransactionClick
            )
        }
    }
}

@Composable
private fun HomeContent(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Summary Section
        item {
            SummaryCards(transactions)
        }

        // Recent Transactions Header
        item {
            Text(
                text = "Recent Transactions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 20.sp
            )
        }

        // Transaction List
        items(transactions.take(10)) { transaction ->
            TransactionListItem(
                transaction = transaction,
                onClick = { onTransactionClick(transaction) }
            )
        }
    }
}

@Composable
private fun SummaryCards(transactions: List<Transaction>) {
    val income = transactions
        .filter { it.type == TransactionType.CREDIT }
        .sumOf { it.amount }

    val expenses = transactions
        .filter { it.type == TransactionType.DEBIT }
        .sumOf { it.amount }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(
            title = "Income",
            amount = String.format("$%.2f", income),
            backgroundColor = SurfaceLighter,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Expenses",
            amount = String.format("$%.2f", expenses),
            backgroundColor = SurfaceLighter,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                fontSize = 14.sp
            )

            Text(
                text = amount,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 28.sp
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                text = "${if (isCredit) "+" else "-"}${String.format("%.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) CreditGreen else DebitRed,
                fontSize = 16.sp
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Transactions Yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Get started by importing your existing bank SMS",
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
        ) {
            Text(
                "Import SMS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}