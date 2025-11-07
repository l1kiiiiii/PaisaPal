package com.example.paisapal.ui.screens.imports


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📬",
                fontSize = 64.sp
            )

            Text(
                text = "Import Your SMS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "PaisaPal will read your existing bank SMS messages and add them to the transaction history.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = importState) {
                ImportState.Idle -> {
                    Button(
                        onClick = { viewModel.importBankSmsOnly() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(
                            "Import Bank SMS (Last 30 Days)",
                            style = MaterialTheme.typography.titleMedium
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
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                ImportState.Loading -> {
                    CircularProgressIndicator()
                    Text("Importing SMS...", style = MaterialTheme.typography.bodyLarge)
                }

                is ImportState.Success -> {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "✅ Import Complete!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Imported: ${state.imported}", style = MaterialTheme.typography.bodyMedium)
                            Text("Failed: ${state.failed}", style = MaterialTheme.typography.bodyMedium)
                            Text("Total: ${state.total}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Button(
                        onClick = onBackClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Done")
                    }
                }

                is ImportState.Error -> {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "❌ Import Failed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(state.message, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Button(
                        onClick = { viewModel.importBankSmsOnly() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun IconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.material3.IconButton(onClick = onClick) {
        content()
    }
}
