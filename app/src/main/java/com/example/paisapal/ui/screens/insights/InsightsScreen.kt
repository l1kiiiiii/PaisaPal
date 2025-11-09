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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.components.CompactTopBar
import com.example.paisapal.ui.theme.*
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.insightsState.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CompactTopBar("Insights")
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Time Frame Toggle
            TimeFrameSelector(
                selectedTimeFrame = state.timeFrame,
                onTimeFrameChange = { viewModel.setTimeFrame(it) }
            )

            // Total Spent Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Total Spent",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "₹${String.format("%.2f", state.totalSpent)}",
                        fontWeight = FontWeight.Bold,
                        color = DebitRed,
                        fontSize = 32.sp
                    )
                    Text(
                        state.timeFrame.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Spending by Category Chart
            if (state.categorySpending.isNotEmpty()) {
                ChartCard(
                    title = "Spending by Category"
                ) {
                    CategoryBarChart(data = state.categorySpending)
                }
            }

            // Daily Spending Trend
            if (state.dailySpending.isNotEmpty()) {
                ChartCard(
                    title = "Spending Trend"
                ) {
                    DailyLineChart(data = state.dailySpending)
                }
            }

            // Category Breakdown List
            if (state.categorySpending.isNotEmpty()) {
                CategoryBreakdownList(categories = state.categorySpending)
            }
        }
    }
}

@Composable
private fun TimeFrameSelector(
    selectedTimeFrame: TimeFrame,
    onTimeFrameChange: (TimeFrame) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFrame.values().forEach { timeFrame ->
            FilterChip(
                selected = timeFrame == selectedTimeFrame,
                onClick = { onTimeFrameChange(timeFrame) },
                label = { Text(timeFrame.name.lowercase().replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryBlue,
                    selectedLabelColor = Color.White,
                    containerColor = SurfaceDark,
                    labelColor = TextGray
                )
            )
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    content: @Composable () -> Unit
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
                text = title,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 18.sp
            )
            content()
        }
    }
}

@Composable
private fun CategoryBarChart(data: List<CategorySpending>) {
    val chartEntryModel = entryModelOf(
        *data.map { it.amount.toFloat() }.toTypedArray()
    )

    Chart(
        chart = columnChart(),
        model = chartEntryModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

@Composable
private fun DailyLineChart(data: List<DailySpending>) {
    val chartEntryModel = entryModelOf(
        *data.map { it.amount.toFloat() }.toTypedArray()
    )

    Chart(
        chart = lineChart(),
        model = chartEntryModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    )
}

@Composable
private fun CategoryBreakdownList(categories: List<CategorySpending>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Category Breakdown",
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                fontSize = 18.sp
            )

            categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        category.category,
                        color = TextWhite,
                        fontSize = 14.sp
                    )
                    Text(
                        "₹${String.format("%.2f", category.amount)}",
                        fontWeight = FontWeight.Bold,
                        color = DebitRed,
                        fontSize = 14.sp
                    )
                }
                if (category != categories.last()) {
                    HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                }
            }
        }
    }
}
