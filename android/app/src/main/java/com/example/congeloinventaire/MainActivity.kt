package com.example.congeloinventaire

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.congeloinventaire.theme.CongeloInventaireTheme
import com.example.congeloinventaire.ui.screens.HomeScreen
import com.example.congeloinventaire.ui.screens.InventoryScreen
import com.example.congeloinventaire.ui.screens.RecipesScreen
import com.example.congeloinventaire.ui.screens.ScannerScreen
import com.example.congeloinventaire.viewmodel.InventoryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: InventoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CongeloInventaireTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val mainDestinations = listOf(
                    MainDestination("home", "Accueil", Icons.Filled.Home),
                    MainDestination("inventory", "Inventaire", Icons.AutoMirrored.Filled.List),
                    MainDestination("recipes", "Recettes", Icons.Filled.Restaurant)
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (mainDestinations.any { it.route == currentRoute }) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 0.dp
                            ) {
                                mainDestinations.forEach { destination ->
                                    NavigationBarItem(
                                        selected = currentRoute == destination.route,
                                        onClick = {
                                            if (destination.route == "recipes" && currentRoute != "recipes") {
                                                viewModel.loadRecipes()
                                            }
                                            navController.navigate(destination.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            androidx.compose.material3.Icon(
                                                imageVector = destination.icon,
                                                contentDescription = destination.label
                                            )
                                        },
                                        label = { Text(destination.label) },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { outerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(outerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToScanner = { isAdding -> navController.navigate("scanner/$isAdding") }
                            )
                        }
                        composable("inventory") {
                            InventoryScreen(
                                viewModel = viewModel,
                                onAddItem = { navController.navigate("scanner/true") }
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
                            RecipesScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

private data class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)
