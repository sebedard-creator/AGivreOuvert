package com.example.congeloinventaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.congeloinventaire.theme.CongeloInventaireTheme
import com.example.congeloinventaire.ui.screens.HomeScreen
import com.example.congeloinventaire.ui.screens.InventoryScreen
import com.example.congeloinventaire.ui.screens.RecipesScreen
import com.example.congeloinventaire.ui.screens.ScannerScreen
import com.example.congeloinventaire.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {
    
    // Instanciation globale du ViewModel
    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CongeloInventaireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToInventory = { navController.navigate("inventory") },
                                onNavigateToScanner = { isAdding -> navController.navigate("scanner/$isAdding") },
                                onNavigateToRecipes = { navController.navigate("recipes") }
                            )
                        }
                        composable("inventory") {
                            InventoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "scanner/{isAdding}",
                            arguments = listOf(navArgument("isAdding") { type = NavType.BoolType })
                        ) { backStackEntry ->
                            val isAdding = backStackEntry.arguments?.getBoolean("isAdding") ?: true
                            ScannerScreen(
                                viewModel = viewModel,
                                isAdding = isAdding,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("recipes") {
                            RecipesScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
