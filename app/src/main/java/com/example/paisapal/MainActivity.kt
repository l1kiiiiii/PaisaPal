package com.example.paisapal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.paisapal.ui.screens.accounts.ManageAccountsViewModel
import com.example.paisapal.ui.screens.onboarding.OnboardingAccountSetup
import com.example.paisapal.ui.theme.PaisaPalTheme
import dagger.hilt.android.AndroidEntryPoint

// ✅ MOVED OUTSIDE MainActivity
enum class PermissionStep {
    SMS_PERMISSION,
    LOCATION_PERMISSION,
    NOTIFICATION_ACCESS,
    ACCOUNT_SETUP,
    ALL_GRANTED
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _hasSmsPermissions = mutableStateOf(false)
    private val _hasLocationPermission = mutableStateOf(false)
    private val _hasNotificationAccess = mutableStateOf(false)

    private val currentStep = mutableStateOf(PermissionStep.SMS_PERMISSION)

    // SMS Permission Launcher
    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "SMS permissions result: $permissions")

        val receiveGranted = permissions[Manifest.permission.RECEIVE_SMS] ?: false
        val readGranted = permissions[Manifest.permission.READ_SMS] ?: false

        val granted = receiveGranted && readGranted

        Log.d(TAG, "SMS Permissions granted: $granted")
        _hasSmsPermissions.value = granted

        if (granted) {
            currentStep.value = PermissionStep.LOCATION_PERMISSION
        }
    }

    // Location Permission Launcher - Only COARSE is mandatory
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Location permissions result: $permissions")

        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        val hasMinimumLocation = coarseGranted

        Log.d(TAG, "Location Permission granted: $hasMinimumLocation (Fine: $fineGranted, Coarse: $coarseGranted)")
        _hasLocationPermission.value = hasMinimumLocation

        if (hasMinimumLocation) {
            currentStep.value = PermissionStep.NOTIFICATION_ACCESS
        }
    }

    // Notification Settings Launcher
    private val notificationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val hasAccess = hasNotificationAccess()
        _hasNotificationAccess.value = hasAccess

        if (hasAccess) {
            currentStep.value = PermissionStep.ACCOUNT_SETUP
        }

        Log.d(TAG, "Notification Access granted: $hasAccess")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")

        checkAllPermissions()

        setContent {
            PaisaPalTheme {
                AppRoot(
                    currentStep = currentStep.value,
                    hasSmsPermissions = _hasSmsPermissions.value,
                    hasLocationPermission = _hasLocationPermission.value,
                    hasNotificationAccess = _hasNotificationAccess.value,
                    onRequestSmsPermission = { requestSmsPermissions() },
                    onRequestLocationPermission = { requestLocationPermissions() },
                    onRequestNotificationAccess = { requestNotificationAccess() },
                    onAccountSetupComplete = {
                        currentStep.value = PermissionStep.ALL_GRANTED
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recheck notification access when returning from settings
        if (_hasSmsPermissions.value && _hasLocationPermission.value) {
            val hasAccess = hasNotificationAccess()
            _hasNotificationAccess.value = hasAccess
            if (hasAccess && currentStep.value == PermissionStep.NOTIFICATION_ACCESS) {
                currentStep.value = PermissionStep.ACCOUNT_SETUP
            }
        }
    }

    private fun checkAllPermissions() {
        try {
            // Check SMS permissions
            val hasSms = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_SMS
                    ) == PackageManager.PERMISSION_GRANTED

            _hasSmsPermissions.value = hasSms
            Log.d(TAG, "SMS permission check: $hasSms")

            // Check Location - only COARSE is mandatory
            val hasFineLocation = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasCoarseLocation = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val hasLocation = hasCoarseLocation
            _hasLocationPermission.value = hasLocation
            Log.d(TAG, "Location permission check: $hasLocation (Fine: $hasFineLocation, Coarse: $hasCoarseLocation)")

            // Check Notification Access
            val hasNotif = hasNotificationAccess()
            _hasNotificationAccess.value = hasNotif
            Log.d(TAG, "Notification access check: $hasNotif")

            // Determine current step
            currentStep.value = when {
                !hasSms -> PermissionStep.SMS_PERMISSION
                !hasLocation -> PermissionStep.LOCATION_PERMISSION
                !hasNotif -> PermissionStep.NOTIFICATION_ACCESS
                else -> PermissionStep.ACCOUNT_SETUP
            }

            Log.d(TAG, "Current permission step: ${currentStep.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions", e)
            _hasSmsPermissions.value = false
            currentStep.value = PermissionStep.SMS_PERMISSION
        }
    }

    private fun hasNotificationAccess(): Boolean {
        return try {
            val enabledListeners = Settings.Secure.getString(
                contentResolver,
                "enabled_notification_listeners"
            )
            enabledListeners?.contains(packageName) == true
        } catch (e: Exception) {
            Log.e(TAG, "Error checking notification access", e)
            false
        }
    }

    private fun requestSmsPermissions() {
        Log.d(TAG, "Requesting SMS permissions")
        smsPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS
            )
        )
    }

    private fun requestLocationPermissions() {
        Log.d(TAG, "Requesting Location permissions")
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun requestNotificationAccess() {
        Log.d(TAG, "Opening Notification Access settings")
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        notificationSettingsLauncher.launch(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

// ✅ AppRoot composable
@Composable
fun AppRoot(
    currentStep: PermissionStep,
    hasSmsPermissions: Boolean,
    hasLocationPermission: Boolean,
    hasNotificationAccess: Boolean,
    onRequestSmsPermission: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onAccountSetupComplete: () -> Unit,
    accountsViewModel: ManageAccountsViewModel = hiltViewModel()
) {
    val accounts by accountsViewModel.accounts.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        when {
            // Step 1: SMS Permission
            currentStep == PermissionStep.SMS_PERMISSION || !hasSmsPermissions -> {
                SmsPermissionScreen(onRequestPermission = onRequestSmsPermission)
            }
            // Step 2: Location Permission
            currentStep == PermissionStep.LOCATION_PERMISSION || !hasLocationPermission -> {
                LocationPermissionScreen(onRequestPermission = onRequestLocationPermission)
            }
            // Step 3: Notification Access
            currentStep == PermissionStep.NOTIFICATION_ACCESS || !hasNotificationAccess -> {
                NotificationAccessScreen(onRequestPermission = onRequestNotificationAccess)
            }
            // Step 4: Account Setup (if no accounts exist)
            currentStep == PermissionStep.ACCOUNT_SETUP || accounts.isEmpty() -> {
                OnboardingAccountSetup(
                    viewModel = accountsViewModel,
                    onComplete = onAccountSetupComplete
                )
            }
            // Step 5: All done - show main app
            else -> {
                MainScreen()
            }
        }
    }
}

@Composable
fun SmsPermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "💬",
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
                text = "Grant SMS Permission",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun SmsPermissionScreenPreview() {
    SmsPermissionScreen(onRequestPermission = {})
}

@Composable
fun LocationPermissionScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📍",
            fontSize = 64.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Location Permission Required",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PaisaPal uses your location to automatically categorize transactions based on where you make payments.",
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
                    "✓ Smart merchant detection",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
                Text(
                    "✓ Auto-categorize by location",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
                Text(
                    "✓ Private & secure on device",
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
                text = "Grant Location Permission",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun LocationPermissionScreenPreview() {
    LocationPermissionScreen(onRequestPermission = {})
}

@Composable
fun NotificationAccessScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔔",
            fontSize = 64.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Notification Access Required",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "PaisaPal needs to read payment app notifications to track UPI transactions in real-time.",
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
                    "✓ Tracks GPay, PhonePe, Paytm",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
                Text(
                    "✓ Reads only payment notifications",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
                Text(
                    "✓ Private & secure on your device",
                    fontSize = 14.sp,
                    color = Color(0xFF00C853)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2D2D2D)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "How to Enable:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "1. Tap the button below",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC)
                )
                Text(
                    "2. Find 'PaisaPal' in the list",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC)
                )
                Text(
                    "3. Toggle the switch ON",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC)
                )
                Text(
                    "4. Return to PaisaPal",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC)
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
                text = "Open Settings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview
@Composable
fun NotificationAccessScreenPreview() {
    NotificationAccessScreen(onRequestPermission = {})
}
