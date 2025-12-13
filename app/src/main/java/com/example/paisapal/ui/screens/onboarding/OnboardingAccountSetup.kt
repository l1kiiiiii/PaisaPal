package com.example.paisapal.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
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
import com.example.paisapal.ui.screens.accounts.ManageAccountsViewModel
import com.example.paisapal.ui.theme.*

@Composable
fun OnboardingAccountSetup(
    viewModel: ManageAccountsViewModel,
    onComplete: () -> Unit
) {
    var currentAccountName by remember { mutableStateOf("") }
    var currentAccountDigits by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var addedAccounts by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Icon
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                "Welcome to PaisaPal!",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle
            Text(
                "Add your bank accounts to get started.",
                color = TextGray,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Text(
                "We only need the last 4 digits for verification.",
                color = TextGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Added Accounts List
            if (addedAccounts.isNotEmpty()) {
                Text(
                    "Added Accounts (${addedAccounts.size})",
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(addedAccounts) { account ->
                        AddedAccountCard(
                            digits = account.first,
                            name = account.second,
                            onRemove = {
                                addedAccounts = addedAccounts.filter { it != account }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Add Account",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Account Name Input
                    OutlinedTextField(
                        value = currentAccountName,
                        onValueChange = {
                            currentAccountName = it
                            error = null
                        },
                        label = { Text("Account Name (Optional)", color = TextGray, fontSize = 12.sp) },
                        placeholder = { Text("e.g., HDFC Salary", color = TextGray.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = TextGray,
                            cursorColor = PrimaryBlue,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Account Digits Input
                    OutlinedTextField(
                        value = currentAccountDigits,
                        onValueChange = {
                            if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                currentAccountDigits = it
                                error = null
                            }
                        },
                        label = { Text("Last 4 Digits *", color = TextGray, fontSize = 12.sp) },
                        placeholder = { Text("1234", color = TextGray.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = error != null,
                        supportingText = error?.let {
                            { Text(it, color = DebitRed, fontSize = 12.sp) }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = TextGray,
                            cursorColor = PrimaryBlue,
                            errorBorderColor = DebitRed,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextGray
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (currentAccountDigits.length == 4) {
                                IconButton(
                                    onClick = {
                                        if (addedAccounts.any { it.first == currentAccountDigits }) {
                                            error = "Account already added"
                                        } else {
                                            addedAccounts = addedAccounts + (currentAccountDigits to currentAccountName)
                                            currentAccountDigits = ""
                                            currentAccountName = ""
                                            error = null
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Account",
                                        tint = PrimaryBlue
                                    )
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button
            Button(
                onClick = {
                    if (addedAccounts.isEmpty()) {
                        error = "Please add at least one account"
                    } else {
                        // Save all accounts
                        addedAccounts.forEach { (digits, name) ->
                            viewModel.addAccount(
                                digits,
                                name.takeIf { it.isNotBlank() }
                            )
                        }
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = TextGray
                ),
                enabled = addedAccounts.isNotEmpty()
            ) {
                Text(
                    if (addedAccounts.isEmpty()) {
                        "Add at least one account to continue"
                    } else {
                        "Continue with ${addedAccounts.size} account${if (addedAccounts.size > 1) "s" else ""}"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Help text
            Text(
                "💡 You can add or remove accounts later in Settings",
                color = TextGray.copy(alpha = 0.7f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddedAccountCard(
    digits: String,
    name: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = name.ifEmpty { "Account" },
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "****$digits",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = TextGray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
