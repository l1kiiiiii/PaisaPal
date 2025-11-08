package com.example.paisapal.ui.screens.imports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSmsScreen(
    viewModel: ImportSmsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {}
) {
    val importState by viewModel.importState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import SMS") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = TextWhite
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Text(
                text = "📬",
                fontSize = 64.sp
            )

            // Title
            Text(
                text = "Import Your SMS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Description
            Text(
                text = "PaisaPal will read your existing bank SMS messages and add them to the transaction history.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = TextGray,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // State-based UI
            when (val state = importState) {
                ImportState.Idle -> {
                    // Import buttons
                    Button(
                        onClick = { viewModel.importBankSmsOnly() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
                    ) {
                        Text(
                            "Import Bank SMS (Last 30 Days)",
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.importAllSms() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            "Import All SMS",
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                    }
                }

                ImportState.Loading -> {
                    CircularProgressIndicator(color = PrimaryGreenLight)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Importing SMS...",
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                }

                is ImportState.Success -> {
                    // Success card with duplicates info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CreditGreen.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "✓ Import Complete!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = CreditGreen
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerColor
                            )

                            // Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Imported:", fontSize = 14.sp, color = TextGray)
                                Text(
                                    "${state.imported} transactions",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CreditGreen
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duplicates skipped:", fontSize = 14.sp, color = TextGray)
                                Text(
                                    "${state.duplicates}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WarningOrange
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Failed to parse:", fontSize = 14.sp, color = TextGray)
                                Text(
                                    "${state.failed}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DebitRed
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = DividerDefaults.Thickness,
                                color = DividerColor
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total SMS scanned:", fontSize = 14.sp, color = TextGray)
                                Text(
                                    "${state.total}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Done button
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
                    ) {
                        Text("Done", fontSize = 16.sp, color = TextWhite)
                    }
                }

                is ImportState.Error -> {
                    // Error card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DebitRed.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "❌ Import Failed",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DebitRed
                            )
                            Text(
                                state.message,
                                fontSize = 14.sp,
                                color = TextWhite,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Retry button
                    Button(
                        onClick = { viewModel.importBankSmsOnly() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
                    ) {
                        Text("Retry", fontSize = 16.sp, color = TextWhite)
                    }
                }
            }
        }
    }
}
