package com.example.protein

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.protein.ui.HomeScreen
import com.example.protein.ui.LogScreen
import com.example.protein.ui.MainViewModel
import com.example.protein.ui.ProteinTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProteinTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                                label = { Text("Today") },
                                selected = currentRoute == "home",
                                onClick = { navController.navigate("home") { launchSingleTop = true } }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.DateRange, contentDescription = "History") },
                                label = { Text("History") },
                                selected = currentRoute == "history",
                                onClick = { /* Phase 2 */ }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.List, contentDescription = "Foods") },
                                label = { Text("Foods") },
                                selected = currentRoute == "foods",
                                onClick = { /* Phase 2 */ }
                            )
                            NavigationBarItem(
                                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                label = { Text("Settings") },
                                selected = currentRoute == "settings",
                                onClick = { /* Phase 2 */ }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController, startDestination = "home", Modifier.padding(innerPadding)) {
                        composable("home") { HomeScreen(viewModel) { navController.navigate("log") } }
                        composable("log") { LogScreen(viewModel) { navController.popBackStack() } }
                    }
                }
            }
        }
    }
}
