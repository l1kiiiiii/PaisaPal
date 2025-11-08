package com.example.paisapal.ui.screens.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.model.Transaction
import com.example.paisapal.ui.screens.home.HomeViewModel
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onCategorizeClick: (Transaction) -> Unit = {}
) {
    val transactions by viewModel.transactions.collectAsState()
    val uncategorized = transactions.filter { it.needsReview }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Transactions") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        }
    ) { paddingValues ->

        if (uncategorized.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "   1 ✅",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "All Caught Up!",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "All transactions are categorized",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Needs Review (${uncategorized.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                items(uncategorized) { transaction ->
                    ReviewTransactionCard(
                        transaction = transaction,
                        onCategorizeClick = { onCategorizeClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewTransactionCard(
    transaction: Transaction,
    onCategorizeClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        transaction.merchantDisplayName ?: "Unknown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        "Rs. ${String.format("%.2f", transaction.amount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }

                Button(
                    onClick = onCategorizeClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreenLight
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Categorize", fontSize = 12.sp)
                }
            }
        }
    }
}
