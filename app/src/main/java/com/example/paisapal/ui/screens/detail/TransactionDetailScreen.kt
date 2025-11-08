package com.example.paisapal.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.paisapal.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transaction: Transaction,
    onBackClick: () -> Unit = {},
    onCategoryChange: (String) -> Unit = {}
) {
    val isCredit = transaction.type == TransactionType.CREDIT

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isCredit) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0xFFE53935).copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = if (isCredit) Icons.Default.ArrowBack else Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = if (isCredit) CreditGreen else DebitRed,
                            modifier = Modifier
                                .size(60.dp)
                                .padding(12.dp)
                        )
                    }

                    Text(
                        text = "${if (isCredit) "+" else "-"}Rs. ${String.format("%.2f", transaction.amount)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) CreditGreen else DebitRed
                    )

                    Text(
                        text = transaction.type.name,
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
            }

            // Details Section
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                DetailRow("Merchant", transaction.merchantDisplayName ?: transaction.merchantRaw ?: "Unknown")
                DetailRow("Date", formatDate(transaction.timestamp))
                DetailRow("Time", formatTime(transaction.timestamp))
                transaction.referenceNumber?.let {
                    DetailRow("Reference No.", it)
                }
                transaction.upiVpa?.let {
                    DetailRow("UPI ID", it)
                }
            }

            // Category Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceLighter)
                ) {
                    Text(
                        transaction.category ?: "Select Category",
                        color = TextWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = TextGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = TextWhite,
            fontSize = 16.sp
        )
        Divider(color = DividerColor, thickness = 0.5.dp, modifier = Modifier.padding(top = 8.dp))
    }
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}
