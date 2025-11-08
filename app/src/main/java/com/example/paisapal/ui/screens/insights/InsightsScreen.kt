package com.example.paisapal.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen() {
    var selectedPeriod by remember { mutableStateOf("Monthly") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryGreen)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Period Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Daily", "Weekly", "Monthly").forEach { period ->
                    Button(
                        onClick = { selectedPeriod = period },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == period) PrimaryGreenLight else SurfaceDark
                        )
                    ) {
                        Text(period)
                    }
                }
            }

            // Spending Trend Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text("Spending Trend", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextGray)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("$1,250", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            Text("Last 3 Months +15%", fontSize = 12.sp, color = CreditGreen)
                        }
                    }

                    // Simple Line Chart Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(BackgroundDark, RoundedCornerShape(8.dp))
                    ) {
                        Text("📈 Chart", modifier = Modifier.align(Alignment.Center), color = TextGray)
                    }
                }
            }

            // Spending Breakdown
            Text("Spending Breakdown", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextWhite)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Category Spending", fontSize = 14.sp, color = TextGray)
                    Text("$1,250", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("This Month +15%", fontSize = 12.sp, color = CreditGreen)

                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryBar("Food", 0.35f)
                    CategoryBar("Shopping", 0.25f)
                    CategoryBar("Transport", 0.25f)
                    CategoryBar("Entertainment", 0.15f)
                }
            }
        }
    }
}

@Composable
private fun CategoryBar(
    category: String,
    percentage: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category, fontSize = 12.sp, color = TextGray)
            Text("${(percentage * 100).toInt()}%", fontSize = 12.sp, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(DividerColor, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(PrimaryGreenLight, RoundedCornerShape(4.dp))
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
