package com.example.paisapal.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.paisapal.ui.screens.home.HomeScreen
import com.example.paisapal.ui.screens.imports.ImportSmsScreen

@Composable
fun PaisaPalNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onImportClick = {
                    navController.navigate("import_sms")
                }
            )
        }

        composable("import_sms") {
            ImportSmsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

object NavRoutes {
    const val HOME = "home"
    const val IMPORT_SMS = "import_sms"
}
