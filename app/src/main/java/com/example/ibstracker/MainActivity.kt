package com.example.ibstracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ibstracker.ui.screens.DashboardScreen
import com.example.ibstracker.ui.screens.FoodScreen
import com.example.ibstracker.ui.screens.SymptomsScreen
import com.example.ibstracker.ui.theme.IBSTrackerTheme

sealed class Screen(
    val route: String,
    val resourceId: Int,
    val icon: ImageVector
) {
    object Dashboard : Screen("dashboard", R.string.dashboard, Icons.Filled.Assessment)
    object Food : Screen("food", R.string.food, Icons.Filled.Fastfood)
    object Symptoms : Screen("symptoms", R.string.symptoms, Icons.Filled.MedicalServices)
}

val items = listOf(
    Screen.Dashboard,
    Screen.Food,
    Screen.Symptoms,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IBSTrackerTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(stringResource(screen.resourceId)) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController, startDestination = Screen.Dashboard.route, Modifier.padding(innerPadding)) {
                        composable(Screen.Dashboard.route) { DashboardScreen() }
                        composable(Screen.Food.route) { FoodScreen() }
                        composable(Screen.Symptoms.route) { SymptomsScreen() }
                    }
                }
            }
        }
    }
}
