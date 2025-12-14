// app/src/main/java/com/example/paisapal/ui/screens/accounts/ManageAccountsScreen.kt
package com.example.paisapal.ui.screens.accounts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.domain.repository.UserAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAccountsScreen(
    viewModel: ManageAccountsViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    // ✅ NEW PARAMETERS FOR ONBOARDING MODE
    isOnboarding: Boolean = false,
    onDoneClick: () -> Unit = {}
) {
    val accounts by viewModel.accounts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                // ✅ DYNAMIC TITLE BASED ON MODE
                title = {
                    Text(
                        text = if (isOnboarding) "Setup Accounts" else "My Accounts",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    // ✅ HIDE BACK BUTTON DURING ONBOARDING
                    if (!isOnboarding) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    // ✅ SHOW 'DONE' BUTTON ONLY IN ONBOARDING MODE WITH ACCOUNTS
                    if (isOnboarding && accounts.isNotEmpty()) {
                        TextButton(onClick = onDoneClick) {
                            Text(
                                "Done",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2196F3)
            ) {
                Icon(Icons.Default.Add, "Add Account", tint = Color.White)
            }
        }
    ) { padding ->
        if (accounts.isEmpty()) {
            // ✅ EMPTY STATE WITH CONTEXT-AWARE MESSAGE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Accounts Added",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isOnboarding) {
                        "Add at least one account to continue"
                    } else {
                        "Add your bank account last 4 digits to authenticate transactions"
                    },
                    color = if (isOnboarding) Color(0xFFFF9800) else Color.Gray,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            // ✅ ACCOUNT LIST
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts) { account ->
                    AccountCard(
                        account = account,
                        onDelete = { viewModel.removeAccount(account.last4Digits) }
                    )
                }

                // ✅ ONBOARDING HINT AT BOTTOM
                if (isOnboarding) {
                    item {
                        Text(
                            "💡 Tap 'Done' when finished adding accounts",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // ✅ ADD ACCOUNT DIALOG
    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { digits, name ->
                viewModel.addAccount(digits, name)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AccountCard(
    account: UserAccount,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = account.accountName ?: "Account",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "****${account.last4Digits}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFFF5252))
            }
        }
    }
}

@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit
) {
    var digits by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name (Optional)") },
                    placeholder = { Text("e.g., HDFC Salary") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = digits,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            digits = it
                            error = null
                        }
                    },
                    label = { Text("Last 4 Digits *") },
                    placeholder = { Text("1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = Color.Red) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        digits.isEmpty() -> error = "Please enter digits"
                        digits.length != 4 -> error = "Must be exactly 4 digits"
                        else -> onAdd(digits, name.takeIf { it.isNotBlank() })
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
