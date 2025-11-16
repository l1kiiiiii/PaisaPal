// app/src/main/java/com/example/paisapal/MainActivity.kt
package com.example.paisapal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paisapal.ui.theme.PaisaPalTheme
import com.example.paisapal.util.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)

        setContent {
            PaisaPalTheme {
                var showPermissionDialog by remember { mutableStateOf(false) }
                var hasLocationPermission by remember {
                    mutableStateOf(permissionManager.hasLocationPermission(this))
                }

                // Check permissions on first launch
                LaunchedEffect(Unit) {
                    if (!hasLocationPermission) {
                        showPermissionDialog = true
                    }
                }

                // Main app content
                MainScreen()

                // Permission dialog
                if (showPermissionDialog) {
                    LocationPermissionDialog(
                        onDismiss = { showPermissionDialog = false },
                        onConfirm = {
                            permissionManager.requestLocationPermission { granted ->
                                hasLocationPermission = granted
                                showPermissionDialog = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationPermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.LocationOn,
                contentDescription = null
            )
        },
        title = {
            Text("Location Permission Required")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "PaisaPal needs location access to:"
                )
                Text("• Detect offline payments at physical stores")
                Text("• Auto-categorize transactions based on your location")
                Text("• Link payments to saved places")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your location is only used when you make a payment and is never shared with third parties.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}
