package com.example.paisapal.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.paisapal.ui.theme.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onImportClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {},
    onReviewClick: () -> Unit = {},
    onQuickAddClick: () -> Unit = {}
) {
    val homeFeed by viewModel.homeFeed.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CompactTopBar("PaisaPal")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onQuickAddClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Quick Add")
            }
        }
    ) { paddingValues ->
        if (homeFeed.isEmpty()) {
            EmptyStateWithImport(
                modifier = Modifier.padding(paddingValues),
                onImportClick = onImportClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(homeFeed, key = { item ->
                    when (item) {
                        is HomeFeedItem.OverviewCard -> "overview"
                        is HomeFeedItem.TransactionItem -> item.transaction.id
                        is HomeFeedItem.BudgetAlert -> "budget_${item.category}"
                        is HomeFeedItem.ReviewPrompt -> "review_${item.transactionId}"
                    }
                }) { feedItem ->
                    when (feedItem) {
                        is HomeFeedItem.OverviewCard -> {
                            OverviewCardComposable(
                                item = feedItem,
                                onReviewClick = onReviewClick
                            )
                        }
                        is HomeFeedItem.TransactionItem -> {
                            TransactionListItem(
                                transaction = feedItem.transaction,
                                onClick = { onTransactionClick(feedItem.transaction) }
                            )
                        }
                        is HomeFeedItem.BudgetAlert -> {
                            BudgetAlertCard(item = feedItem)
                        }
                        is HomeFeedItem.ReviewPrompt -> {
                            ReviewPromptCard(
                                item = feedItem,
                                onReviewClick = onReviewClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewCardComposable(
    item: HomeFeedItem.OverviewCard,
    onReviewClick: () -> Unit
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
                text = "November Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Total Spent
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Spent:", color = TextGray, fontSize = 14.sp)
                Text(
                    "₹${String.format("%.2f", item.totalSpent)}",
                    fontWeight = FontWeight.Bold,
                    color = DebitRed,
                    fontSize = 18.sp
                )
            }

            // Needs Review (Clickable)
            if (item.needsReview > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReviewClick() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Needs Review:", color = TextGray, fontSize = 14.sp)
                    Text(
                        "${item.needsReview} transactions →",
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange,
                        fontSize = 14.sp
                    )
                }
            }

            // Budget Progress
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.budgetStatus} Budget:", color = TextGray, fontSize = 14.sp)
                    Text(
                        "${(item.budgetProgress * 100).toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = if (item.budgetProgress > 0.9f) DebitRed else PrimaryGreen,
                        fontSize = 14.sp
                    )
                }
                LinearProgressIndicator(
                    progress = { item.budgetProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (item.budgetProgress > 0.9f) DebitRed else PrimaryGreen,
                    trackColor = SurfaceLighter,
                )
            }
        }
    }
}

@Composable
private fun BudgetAlertCard(item: HomeFeedItem.BudgetAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DebitRed.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Budget Alert!",
                    fontWeight = FontWeight.Bold,
                    color = DebitRed,
                    fontSize = 16.sp
                )
                Text(
                    "You've exceeded your '${item.category}' budget.",
                    color = TextWhite,
                    fontSize = 14.sp
                )
                Text(
                    "₹${String.format("%.0f", item.spent)} / ₹${String.format("%.0f", item.limit)}",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ReviewPromptCard(
    item: HomeFeedItem.ReviewPrompt,
    onReviewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onReviewClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🤔", fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Help us learn!",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 16.sp
                )
                Text(
                    "We saw a ₹${String.format("%.2f", item.amount)} payment.",
                    color = TextWhite,
                    fontSize = 14.sp
                )
                item.suggestedCategory?.let {
                    Text(
                        "Was this '$it'?",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
            Text(
                "→",
                fontSize = 24.sp,
                color = PrimaryBlue
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
                text = "${if (isCredit) "+" else "-"}₹${String.format("%.2f", transaction.amount)}",
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
