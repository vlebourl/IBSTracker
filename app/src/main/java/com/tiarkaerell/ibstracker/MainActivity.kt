package com.tiarkaerell.ibstracker

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tiarkaerell.ibstracker.ui.screens.AnalysisScreen
import com.tiarkaerell.ibstracker.ui.screens.BackupSettingsScreen
import com.tiarkaerell.ibstracker.ui.screens.DashboardScreen
import com.tiarkaerell.ibstracker.ui.screens.FoodScreen
import com.tiarkaerell.ibstracker.ui.screens.SettingsScreen
import com.tiarkaerell.ibstracker.ui.screens.SymptomsScreen
import com.tiarkaerell.ibstracker.ui.theme.IBSTrackerTheme
import com.tiarkaerell.ibstracker.ui.viewmodel.AnalyticsViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.BackupViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.SettingsViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.SymptomsViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.ViewModelFactory
import com.tiarkaerell.ibstracker.utils.LocaleHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

import com.tiarkaerell.ibstracker.R

sealed class Screen(val route: String, val titleRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", R.string.nav_dashboard, Icons.Filled.Dashboard)
    object Food : Screen("food", R.string.nav_food, Icons.Filled.Fastfood)
    object Symptoms : Screen("symptoms", R.string.nav_symptoms, Icons.Filled.Medication)
    object Analytics : Screen("analytics", R.string.nav_analytics, Icons.Filled.Analytics)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings)
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // Apply saved language preference BEFORE the activity is created
        val container = (newBase.applicationContext as IBSTrackerApplication).container
        val languageCode = runBlocking {
            container.settingsRepository.languageFlow.first().code
        }
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as IBSTrackerApplication).container

        val viewModelFactory = ViewModelFactory(
            container.dataRepository,
            container.settingsRepository,
            container.analysisRepository,
            container.backupRepository,
            container.authorizationManager,
            container.appContext,
            container.appDatabase
        )

        setContent {
            IBSTrackerTheme {
                MainScreen(viewModelFactory)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModelFactory: ViewModelFactory) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Dashboard,
        Screen.Food,
        Screen.Symptoms,
        Screen.Analytics,
        Screen.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = items.find { it.route == currentDestination?.route }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Hide topBar for sub-screens that have their own header (like backup_settings)
            if (currentDestination?.route != "backup_settings") {
                TopAppBar(
                    title = {
                        Text(
                            if (currentScreen != null) {
                                stringResource(currentScreen.titleRes)
                            } else {
                                stringResource(R.string.app_name)
                            }
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.titleRes)) },
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
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val foodViewModel: FoodViewModel = viewModel(factory = viewModelFactory)
                val symptomsViewModel: SymptomsViewModel = viewModel(factory = viewModelFactory)
                val foodUsageStatsViewModel: com.tiarkaerell.ibstracker.ui.viewmodel.FoodUsageStatsViewModel = viewModel(factory = viewModelFactory)
                DashboardScreen(
                    foodViewModel = foodViewModel,
                    symptomsViewModel = symptomsViewModel,
                    foodUsageStatsViewModel = foodUsageStatsViewModel
                )
            }
            composable(Screen.Food.route) {
                val foodViewModel: FoodViewModel = viewModel(factory = viewModelFactory)
                FoodScreen(foodViewModel = foodViewModel)
            }
            composable(Screen.Symptoms.route) {
                val symptomsViewModel: SymptomsViewModel = viewModel(factory = viewModelFactory)
                SymptomsScreen(symptomsViewModel = symptomsViewModel)
            }
            composable(Screen.Analytics.route) {
                val analyticsViewModel: AnalyticsViewModel = viewModel(factory = viewModelFactory)
                AnalysisScreen(analyticsViewModel = analyticsViewModel)
            }
            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onNavigateToBackupSettings = {
                        navController.navigate("backup_settings")
                    }
                )
            }
            composable("backup_settings") {
                val backupViewModel: BackupViewModel = viewModel(factory = viewModelFactory)
                val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                val backupPassword by settingsViewModel.backupPassword.collectAsState()

                BackupSettingsScreen(
                    viewModel = backupViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    hasBackupPassword = settingsViewModel.hasBackupPassword(),
                    backupPassword = backupPassword,
                    onPasswordChange = { password ->
                        settingsViewModel.setBackupPassword(password)
                    }
                )
            }
        }
    }
}
