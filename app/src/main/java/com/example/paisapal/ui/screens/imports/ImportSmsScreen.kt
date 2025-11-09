package com.example.paisapal.ui.screens.imports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.theme.*
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportSmsScreen(
    viewModel: ImportSmsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onImportComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val importState by viewModel.importState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.acknowledgePermissionExplanation()
        } else {
            Toast.makeText(context, "SMS permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(importState) {
        if (importState is ImportSmsViewModel.ImportState.Idle) {
            if (context.checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import SMS") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryGreen,
                    titleContentColor = TextWhite
                )
            )
        }
    ) { paddingValues ->

        when (importState) {
            is ImportSmsViewModel.ImportState.PermissionExplanation -> PermissionExplanationScreen(
                modifier = Modifier.padding(paddingValues),
                onContinue = { viewModel.acknowledgePermissionExplanation() }
            )
            is ImportSmsViewModel.ImportState.Idle -> ImportOptionsScreen(
                modifier = Modifier.padding(paddingValues),
                onBankSmsClick = { viewModel.importBankSmsOnly() },
                onAllSmsClick = { viewModel.importAllSms() }
            )
            is ImportSmsViewModel.ImportState.Loading -> LiveProgressScreen(
                modifier = Modifier.padding(paddingValues),
                state = importState as ImportSmsViewModel.ImportState.Loading
            )
            is ImportSmsViewModel.ImportState.Success -> SuccessScreen(
                modifier = Modifier.padding(paddingValues),
                state = importState as ImportSmsViewModel.ImportState.Success,
                onDone = onImportComplete
            )
            is ImportSmsViewModel.ImportState.Error -> ErrorScreen(
                modifier = Modifier.padding(paddingValues),
                message = (importState as ImportSmsViewModel.ImportState.Error).message,
                onRetry = { viewModel.importBankSmsOnly() }
            )
        }
    }
}
@Composable
private fun PermissionExplanationScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
    ) {
        Text("🔒", fontSize = 64.sp)

        Text(
            "How PaisaPal Works",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ExplanationItem(
                    icon = "📱",
                    text = "We need to read your SMS inbox to track transactions automatically"
                )
                ExplanationItem(
                    icon = "🏦",
                    text = "We only read messages from banks and financial services"
                )
                ExplanationItem(
                    icon = "🔐",
                    text = "Your personal messages are never read"
                )
                ExplanationItem(
                    icon = "📍",
                    text = "All your data stays on your phone—we never upload anything"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text(
                "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

@Composable
private fun ExplanationItem(icon: String, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(icon, fontSize = 24.sp)
        Text(
            text,
            color = TextWhite,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ImportOptionsScreen(
    onBankSmsClick: () -> Unit,
    onAllSmsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📬", fontSize = 64.sp)

        Text(
            "Import Your SMS",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        Text(
            "PaisaPal will scan your messages and automatically track your transactions",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = TextGray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onBankSmsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
        ) {
            Text("Import Bank SMS (Recommended)", color = androidx.compose.ui.graphics.Color.White)
        }

        OutlinedButton(
            onClick = onAllSmsClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Import All SMS", color = TextWhite)
        }
    }
}

@Composable
private fun LiveProgressScreen(
    state: ImportSmsViewModel.ImportState.Loading,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(80.dp),
            color = PrimaryGreen,
            strokeWidth = 6.dp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            "Scanning your messages...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProgressRow(
                    label = "Messages Scanned",
                    value = "${state.messagesScanned} / ${state.totalMessages}"
                )
                LinearProgressIndicator(
                    progress = {
                        if (state.totalMessages > 0) state.messagesScanned.toFloat() / state.totalMessages
                        else 0f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = PrimaryGreen,
                )

                Spacer(modifier = Modifier.height(8.dp))

                ProgressRow(
                    label = "Transactions Found",
                    value = "${state.transactionsFound}"
                )

                ProgressRow(
                    label = "Categorized",
                    value = "${state.categorized}"
                )
            }
        }
    }
}

@Composable
private fun ProgressRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun SuccessScreen(
    state: ImportSmsViewModel.ImportState.Success,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text("🎉", fontSize = 80.sp)

        Text(
            "Import Complete!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CreditGreen
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatRow("Imported", "${state.imported} transactions", CreditGreen)
                StatRow("Duplicates skipped", "${state.duplicates}", WarningOrange)
                StatRow("Failed to parse", "${state.failed}", DebitRed)
                HorizontalDivider(color = DividerColor)
                StatRow("Total scanned", "${state.total}", TextWhite)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("View Transactions", color = androidx.compose.ui.graphics.Color.White, fontSize = 16.sp)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Text(
            value,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("❌", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Import Failed",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DebitRed
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            message,
            fontSize = 14.sp,
            color = TextWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
        ) {
            Text("Retry", color = androidx.compose.ui.graphics.Color.White)
        }
    }
}
