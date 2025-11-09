package com.example.paisapal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.paisapal.PaisaPalApp
import com.example.paisapal.ui.theme.PaisaPalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ✅ Use mutableStateOf properly
    private val _hasPermissions = mutableStateOf(false)
    private val hasPermissions: Boolean
        get() = _hasPermissions.value

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.RECEIVE_SMS] == true &&
                permissions[Manifest.permission.READ_SMS] == true

        Log.d(TAG, "Permissions granted: $granted")

        // ✅ Update state to trigger recompose
        _hasPermissions.value = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")

        // Check initial permissions
        checkPermissions()

        setContent {
            PaisaPalTheme {
                // ✅ Observe permission state
                val permissionsGranted = _hasPermissions.value

                Log.d(TAG, "Recomposing with permissions: $permissionsGranted")

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    if (permissionsGranted) {
                        // Show main app
                        PaisaPalApp()
                    } else {
                        // Show permission screen
                        PermissionScreen(
                            onRequestPermission = { requestPermissions() }
                        )
                    }
                }
            }
        }
    }

    private fun checkPermissions() {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_SMS
                ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "Initial permission check: $granted")
        _hasPermissions.value = granted
    }

    private fun requestPermissions() {
        Log.d(TAG, "Requesting permissions")
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            )
        )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Composable
fun PermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📱",
            fontSize = 64.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SMS Permission Required",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PaisaPal needs SMS access to automatically track your bank transactions.",
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color(0xFFAAAAAA),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1A1A1A)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "✓ Only reads bank SMS",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
                Text(
                    "✓ No data sent anywhere",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
                Text(
                    "✓ Everything stays on device",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C853)
            )
        ) {
            Text(
                text = "Grant Permission",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
