package com.example.paisapal.ui.screens.categorize

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.screens.detail.TransactionDetailViewModel
import com.example.paisapal.ui.theme.*

data class CategoryItem(
    val name: String,
    val icon: ImageVector,
    val backgroundColor: androidx.compose.ui.graphics.Color
)

val CATEGORIES = listOf(
    CategoryItem("Shopping", Icons.Filled.ShoppingCart, CategoryBgShopping),
    CategoryItem("Transportation", Icons.Filled.DirectionsCar, CategoryBgTransport),
    CategoryItem("Housing", Icons.Filled.Home, androidx.compose.ui.graphics.Color(0xFF8BC34A).copy(alpha = 0.2f)),
    CategoryItem("Food & Dining", Icons.Filled.Restaurant, CategoryBgFood),
    CategoryItem("Income", Icons.Filled.AttachMoney, CategoryBgEducation),
    CategoryItem("Gifts", Icons.Filled.CardGiftcard, androidx.compose.ui.graphics.Color(0xFFE91E63).copy(alpha = 0.2f)),
    CategoryItem("Entertainment", Icons.Filled.MovieCreation, CategoryBgEntertainment),
    CategoryItem("Health & Fitness", Icons.Filled.FavoriteBorder, CategoryBgHealth),
    CategoryItem("Work", Icons.Filled.Work, CategoryBgWork),
    CategoryItem("Education", Icons.Filled.School, CategoryBgEducation),
    CategoryItem("Travel", Icons.Filled.Flight, androidx.compose.ui.graphics.Color(0xFF00BCD4).copy(alpha = 0.2f)),
    CategoryItem("Other", Icons.Filled.MoreHoriz, DisabledColor.copy(alpha = 0.2f))
)

@Composable
fun CategorizeScreen(
    transactionId: String,
    onBackClick: () -> Unit = {},
    onCategorySelected: () -> Unit = {},
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    // Load transaction
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    val transaction by viewModel.transaction.collectAsState()

    if (transaction == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryGreenLight)
        }
    } else {
        val txn = transaction!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextWhite
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Uncategorized",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )

                    Text(
                        "Rs. ${String.format("%.2f", txn.amount)}",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }
            }

            // Category Selection
            Text(
                "Select a category",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(CATEGORIES) { category ->
                    CategoryButton(
                        category = category,
                        onClick = {
                            // Update category and go back
                            onCategorySelected()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryButton(
    category: CategoryItem,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = category.backgroundColor,
            contentColor = TextWhite
        ),
        border = ButtonDefaults.outlinedButtonBorder
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}
