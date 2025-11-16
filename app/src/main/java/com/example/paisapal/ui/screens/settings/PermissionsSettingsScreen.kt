package com.example.paisapal.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.paisapal.ui.theme.*
import com.example.paisapal.util.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsSection(
    permissionManager: PermissionManager
) {
    val context = LocalContext.current
    var hasLocationPermission by remember {
        mutableStateOf(permissionManager.hasLocationPermission(context))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Permissions",
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite
            )

            // Location Permission
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (hasLocationPermission) PrimaryGreenLight else TextGray
                    )
                    Column {
                        Text(
                            "Location Access",
                            color = TextWhite,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            if (hasLocationPermission) "Enabled" else "Disabled",
                            color = TextGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (!hasLocationPermission) {
                    Button(
                        onClick = {
                            permissionManager.requestLocationPermission { granted ->
                                hasLocationPermission = granted
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreenLight
                        )
                    ) {
                        Text("Enable")
                    }
                }
            }

            if (!hasLocationPermission) {
                Surface(
                    color = WarningOrange.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Enable location to auto-detect offline payments",
                            color = TextWhite,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
