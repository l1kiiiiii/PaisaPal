package com.example.paisapal.ui.screens.categorize

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorizeScreen(
    transactionId: String,
    onBackClick: () -> Unit,
    onCategorizeComplete: (String) -> Unit,
    viewModel: CategorizeViewModel = hiltViewModel()
) {
    val categories = listOf(
        "Food & Dining" to "🍔",
        "Shopping" to "🛍️",
        "Transportation" to "🚗",
        "Groceries" to "🛒",
        "Entertainment" to "🎬",
        "Bills & Utilities" to "💡",
        "Healthcare" to "🏥",
        "Education" to "📚",
        "Travel" to "✈️",
        "Transfer" to "💸",
        "Other" to "📌"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Choose Category",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(categories) { (category, emoji) ->
                CategoryCard(
                    category = category,
                    emoji = emoji,
                    onClick = {
                        viewModel.categorizeTransaction(transactionId, category)
                        onCategorizeComplete(category)
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: String,
    emoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                fontSize = 32.sp
            )
            Text(
                text = category,
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
