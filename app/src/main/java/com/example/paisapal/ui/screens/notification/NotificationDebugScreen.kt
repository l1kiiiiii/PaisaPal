package com.example.paisapal.ui.screens.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.domain.data.NotificationCache
import com.example.domain.data.PaymentNotification
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationDebugScreen(
    onBackClick: () -> Unit,
    viewModel: NotificationDebugViewModel = hiltViewModel()
) {
    // Use remember to fetch notifications once
    val notifications = remember { viewModel.getNotifications() }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Notifications (${notifications.size})", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "No notifications cached",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Make a GPay/PhonePe payment to see notifications here",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { notif: PaymentNotification ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            val isCredit = notif.amount >= 0
                            val displayAmount = kotlin.math.abs(notif.amount)
                            val sign = if (isCredit) "+" else "-"
                            val color = if (isCredit) Color(0xFF00C853) else Color(0xFFD32F2F) // Green vs Red
                            Text(
                                text = "₹${String.format("%.2f", notif.amount)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color =color
                            )
                            Text("App: ${notif.appName}", color = Color.White, fontSize = 14.sp)
                            notif.merchantName?.let {
                                Text("Merchant: $it", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class NotificationDebugViewModel @Inject constructor(
    private val notificationCache: NotificationCache
) : ViewModel() {
    fun getNotifications(): List<PaymentNotification> {
        return notificationCache.getRecentNotifications(50)
    }
}
