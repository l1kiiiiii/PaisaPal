package com.example.paisapal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompactTopBar(
    title: String,
    showSettings: Boolean = true,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    //  Remove statusBarsPadding to lower the bar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)  //  Black, not green
            .padding(horizontal = 16.dp, vertical = 12.dp),  //  Reduced padding
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            // Empty spacer if no back button
            Box(modifier = Modifier.size(40.dp))
        }

        // Title (centered)
        Text(
            title,
            color = Color.White,
            fontSize = 20.sp,  //  Slightly larger
            fontWeight = FontWeight.Bold
        )

        // Right side
        if (showSettings) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Box(modifier = Modifier.size(40.dp))
        }
    }
}

