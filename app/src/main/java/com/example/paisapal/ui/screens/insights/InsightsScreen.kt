package com.example.paisapal.ui.screens.insights

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.usecase.InsightsPeriod
import com.example.paisapal.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val insightsData by viewModel.insightsData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = TextWhite
                )
            )
        }
    ) { paddingValues ->

        if (isLoading || insightsData == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundDark)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreenLight)
            }
        } else {
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
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { viewModel.selectPeriod(it) }
                )

                // Summary Cards
                SummaryCards(insightsData!!)

                // Spending Trend Chart
                SpendingTrendCard(insightsData!!)

                // Category Breakdown
                CategoryBreakdownCard(insightsData!!)

                // Top Merchants
                TopMerchantsCard(insightsData!!)
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: InsightsPeriod,
    onPeriodSelected: (InsightsPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PeriodButton("Daily", InsightsPeriod.DAILY, selectedPeriod, onPeriodSelected, Modifier.weight(1f))
        PeriodButton("Weekly", InsightsPeriod.WEEKLY, selectedPeriod, onPeriodSelected, Modifier.weight(1f))
        PeriodButton("Monthly", InsightsPeriod.MONTHLY, selectedPeriod, onPeriodSelected, Modifier.weight(1f))
    }
}

@Composable
private fun PeriodButton(
    label: String,
    period: InsightsPeriod,
    selectedPeriod: InsightsPeriod,
    onPeriodSelected: (InsightsPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { onPeriodSelected(period) },
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selectedPeriod == period) PrimaryGreenLight else SurfaceDark
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(label, color = TextWhite)
    }
}

@Composable
private fun SummaryCards(data: com.example.domain.model.InsightsData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "Total Spent",
            amount = data.totalSpent,
            color = DebitRed,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            title = "Total Income",
            amount = data.totalIncome,
            color = CreditGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                title,
                fontSize = 12.sp,
                color = TextGray
            )

            Text(
                "₹${String.format("%.0f", amount)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun SpendingTrendCard(data: com.example.domain.model.InsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Spending Trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Line Chart
            if (data.monthlyTrend.isNotEmpty()) {
                // Extract amounts as Float array
                val chartValues = data.monthlyTrend.map { it.amount.toFloat() }

                // Create chart model with spread operator
                val chartEntryModel = entryModelOf(*chartValues.toTypedArray())

                Chart(
                    chart = lineChart(),
                    model = chartEntryModel,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data available", color = TextGray)
                }
            }
        }
    }
}


@Composable
private fun CategoryBreakdownCard(data: com.example.domain.model.InsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Category Spending",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "₹${String.format("%.0f", data.totalSpent)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            data.categoryBreakdown.take(5).forEach { category ->
                CategoryBar(category)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryBar(category: com.example.domain.model.CategorySpending) {
    val animatedProgress by animateFloatAsState(
        targetValue = category.percentage / 100f,
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category.category, fontSize = 14.sp, color = TextWhite)
            Text(
                "₹${String.format("%.0f", category.amount)} (${String.format("%.0f", category.percentage)}%)",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(DividerColor, RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(PrimaryGreenLight, RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
private fun TopMerchantsCard(data: com.example.domain.model.InsightsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                "Top Merchants",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            data.topMerchants.forEach { merchant ->
                MerchantRow(merchant)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MerchantRow(merchant: com.example.domain.model.MerchantSpending) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                merchant.merchantName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Text(
                "${merchant.transactionCount} transactions",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        Text(
            "₹${String.format("%.0f", merchant.amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DebitRed
        )
    }
}
