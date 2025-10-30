package com.tiarkaerell.ibstracker.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.auth.AuthorizationManager
import com.tiarkaerell.ibstracker.data.auth.GoogleAuthManager
import com.tiarkaerell.ibstracker.data.auth.rememberGoogleAuthManager
import com.tiarkaerell.ibstracker.data.model.*
import com.tiarkaerell.ibstracker.ui.viewmodel.SettingsViewModel
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToBackupSettings: () -> Unit = {}
) {
    val language by settingsViewModel.language.collectAsState()
    val units by settingsViewModel.units.collectAsState()
    val userProfile by settingsViewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val googleAuthManager = rememberGoogleAuthManager()
    val authState by googleAuthManager.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Authorization manager for Google Drive scopes (new API)
    val authorizationManager = remember { AuthorizationManager(context) }

    // Note: Authentication state is restored instantly in GoogleAuthManager init
    // from EncryptedSharedPreferences - no need for LaunchedEffect

    // Authorization launcher for Google Drive scope consent
    val authorizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        coroutineScope.launch {
            if (result.resultCode == Activity.RESULT_OK && activity != null) {
                try {
                    // Get authorization result and extract access token
                    val authResult = authorizationManager.getAuthorizationFromIntent(activity, result.data)
                    val accessToken = authResult.accessToken

                    // Store token in ViewModel
                    settingsViewModel.handleAuthorizationResult(accessToken)
                } catch (e: Exception) {
                    // Authorization failed
                    settingsViewModel.handleAuthorizationResult(null)
                }
            } else {
                // User cancelled authorization
                settingsViewModel.handleAuthorizationResult(null)
            }
        }
    }

    // Collect authorization events from ViewModel
    LaunchedEffect(settingsViewModel.authorizationEvents) {
        settingsViewModel.authorizationEvents.collect { event ->
            when (event) {
                is SettingsViewModel.AuthorizationEvent.RequestDriveAuthorization -> {
                    if (activity != null) {
                        try {
                            val intentSenderRequest = authorizationManager.requestAuthorization(activity)
                            if (intentSenderRequest != null) {
                                // Need to show consent UI
                                authorizationLauncher.launch(intentSenderRequest)
                            } else {
                                // Already authorized, get token directly
                                val accessToken = authorizationManager.getAccessToken(activity)
                                settingsViewModel.handleAuthorizationResult(accessToken)
                            }
                        } catch (e: Exception) {
                            // Authorization request failed
                            settingsViewModel.handleAuthorizationResult(null)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Preferences Section
        var languageExpanded by remember { mutableStateOf(false) }
        var unitsExpanded by remember { mutableStateOf(false) }

        SettingsSection(
            title = stringResource(R.string.settings_title),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            SettingDropdownItem(
                title = stringResource(R.string.settings_language),
                selectedValue = stringResource(language.displayNameRes),
                icon = Icons.Default.Language,
                expanded = languageExpanded,
                onExpandedChange = { languageExpanded = it },
                dropdownContent = {
                    ExposedDropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }
                    ) {
                        Language.entries.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(stringResource(lang.displayNameRes)) },
                                onClick = {
                                    languageExpanded = false
                                    coroutineScope.launch {
                                        settingsViewModel.setLanguage(lang)
                                        kotlinx.coroutines.delay(100)
                                        activity?.recreate()
                                    }
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            )

            SettingDropdownItem(
                title = stringResource(R.string.settings_units),
                selectedValue = stringResource(units.displayNameRes),
                icon = Icons.Default.Straighten,
                expanded = unitsExpanded,
                onExpandedChange = { unitsExpanded = it },
                dropdownContent = {
                    ExposedDropdownMenu(
                        expanded = unitsExpanded,
                        onDismissRequest = { unitsExpanded = false }
                    ) {
                        Units.entries.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(stringResource(unit.displayNameRes)) },
                                onClick = {
                                    settingsViewModel.setUnits(unit)
                                    unitsExpanded = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Personal Information Section
        PersonalInfoSection(
            userProfile = userProfile,
            units = units,
            onUpdateProfile = { updatedProfile ->
                settingsViewModel.updateUserProfile(updatedProfile)
            }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Backup & Restore Section
        SettingsSection(title = stringResource(R.string.backup_restore_section_title)) {
            SettingNavigationItem(
                title = stringResource(R.string.backup_settings_title),
                description = stringResource(R.string.backup_settings_description),
                icon = Icons.Default.Backup,
                onClick = onNavigateToBackupSettings
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // About Section
        SettingsSection(title = stringResource(R.string.about_section_title)) {
            SettingReadOnlyItem(
                title = stringResource(R.string.version_label),
                value = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown",
                icon = Icons.Default.Info
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GoogleFitIntegrationCard(
    isSignedIn: Boolean,
    hasPermissions: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.health_integration_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.google_fit_integration),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.google_fit_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isSignedIn) {
                    if (hasPermissions) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.google_fit_connected),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            TextButton(onClick = onDisconnect) {
                                Text(stringResource(R.string.disconnect_google_fit))
                            }
                        }
                    } else {
                        Button(onClick = onConnect) {
                            Text(stringResource(R.string.connect_google_fit))
                        }
                    }
                }
            }
            
            if (!isSignedIn) {
                Text(
                    text = stringResource(R.string.sign_in_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInfoSection(
    userProfile: UserProfile,
    units: Units,
    onUpdateProfile: (UserProfile) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    SettingsSection(title = stringResource(R.string.personal_info_title)) {
        Column {
            // Date of Birth
            SettingDialogItem(
                title = stringResource(R.string.date_of_birth_label),
                value = userProfile.dateOfBirth?.let {
                    dateFormat.format(Date(it))
                } ?: stringResource(R.string.not_specified),
                icon = Icons.Default.CalendarToday,
                onClick = { showDatePicker = true }
            )

            // Sex
            SettingDropdownItem(
                title = stringResource(R.string.sex_label),
                selectedValue = stringResource(userProfile.sex.displayNameRes),
                icon = Icons.Default.Person,
                expanded = sexExpanded,
                onExpandedChange = { sexExpanded = it },
                dropdownContent = {
                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        Sex.entries.forEach { sex ->
                            DropdownMenuItem(
                                text = { Text(stringResource(sex.displayNameRes)) },
                                onClick = {
                                    onUpdateProfile(userProfile.copy(sex = sex))
                                    sexExpanded = false }
                            )
                        }
                    }
                }
            )

            // Height
            SettingDialogItem(
                title = stringResource(R.string.height_label),
                value = userProfile.heightCm?.let { height ->
                    if (units == Units.METRIC) {
                        stringResource(R.string.height_cm_format, height)
                    } else {
                        val (feet, inches) = userProfile.getHeightInFeetInches() ?: Pair(0, 0)
                        stringResource(R.string.height_ft_in_format, feet, inches)
                    }
                } ?: stringResource(R.string.not_specified),
                icon = Icons.Default.Height,
                onClick = { showHeightDialog = true }
            )

            // Weight
            SettingDialogItem(
                title = stringResource(R.string.weight_label),
                value = userProfile.weightKg?.let { weight ->
                    if (units == Units.METRIC) {
                        stringResource(R.string.weight_kg_format, weight)
                    } else {
                        val pounds = userProfile.getWeightInPounds() ?: 0
                        stringResource(R.string.weight_lb_format, pounds)
                    }
                } ?: stringResource(R.string.not_specified),
                icon = Icons.Default.MonitorWeight,
                onClick = { showWeightDialog = true }
            )
            
            // Show calculated info if we have basic data
            if (userProfile.hasCompleteBasicInfo()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Age and BMI
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        userProfile.getAge()?.let { age ->
                            Text(
                                text = stringResource(R.string.age_format, age),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        userProfile.getBMI()?.let { bmi ->
                            Text(
                                text = stringResource(R.string.bmi_format, bmi),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(userProfile.getBMICategory().displayNameRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = userProfile.dateOfBirth ?: System.currentTimeMillis()
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dateMillis ->
                        onUpdateProfile(userProfile.copy(dateOfBirth = dateMillis))
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.button_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Height Dialog
    if (showHeightDialog) {
        HeightInputDialog(
            currentHeight = userProfile.heightCm,
            units = units,
            onDismiss = { showHeightDialog = false },
            onConfirm = { heightCm ->
                onUpdateProfile(userProfile.copy(heightCm = heightCm))
                showHeightDialog = false
            }
        )
    }
    
    // Weight Dialog
    if (showWeightDialog) {
        WeightInputDialog(
            currentWeight = userProfile.weightKg,
            units = units,
            onDismiss = { showWeightDialog = false },
            onConfirm = { weightKg ->
                onUpdateProfile(userProfile.copy(weightKg = weightKg))
                showWeightDialog = false
            }
        )
    }
}

@Composable
fun HeightInputDialog(
    currentHeight: Int?,
    units: Units,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var heightText by remember { 
        mutableStateOf(currentHeight?.toString() ?: "") 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.height_label)) },
        text = {
            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { 
                    Text(
                        if (units == Units.METRIC) "Height (cm)" 
                        else "Height (inches)"
                    ) 
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    heightText.toIntOrNull()?.let { height ->
                        val heightCm = if (units == Units.METRIC) {
                            height
                        } else {
                            (height * 2.54).toInt() // Convert inches to cm
                        }
                        onConfirm(heightCm)
                    }
                }
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@Composable
fun WeightInputDialog(
    currentWeight: Float?,
    units: Units,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var weightText by remember { 
        mutableStateOf(
            currentWeight?.let { weight ->
                if (units == Units.METRIC) {
                    "%.1f".format(weight)
                } else {
                    "${(weight * 2.20462).toInt()}"
                }
            } ?: ""
        ) 
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.weight_label)) },
        text = {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { 
                    Text(
                        if (units == Units.METRIC) "Weight (kg)" 
                        else "Weight (lbs)"
                    ) 
                },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    weightText.toFloatOrNull()?.let { weight ->
                        val weightKg = if (units == Units.METRIC) {
                            weight
                        } else {
                            weight / 2.20462f // Convert pounds to kg
                        }
                        onConfirm(weightKg)
                    }
                }
            ) {
                Text(stringResource(R.string.button_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

// BackupPasswordCard moved to BackupSettingsScreen
// LocalFileBackupCard removed - JSON import/export functionality moved to BackupSettingsScreen
// BackupRestoreCard removed - replaced with SettingNavigationItem in main SettingsScreen

// ==================== NEW MATERIAL DESIGN 3 COMPOSABLES ====================

/**
 * Reusable section header for grouping related settings.
 * Follows Material Design 3 typography and spacing guidelines.
 */
@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

/**
 * Setting item that navigates to a sub-screen.
 * Shows chevron icon to indicate navigation.
 */
@Composable
private fun SettingNavigationItem(
    title: String,
    description: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = description?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
    )
}

/**
 * Read-only setting item for displaying information.
 * Used for app version and other non-interactive settings.
 */
@Composable
private fun SettingReadOnlyItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Setting item with dropdown selection.
 * Uses Material Design 3 ExposedDropdownMenuBox within ListItem.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingDropdownItem(
    title: String,
    selectedValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    dropdownContent: @Composable ExposedDropdownMenuBoxScope.() -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                Row(
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .clickable { onExpandedChange(!expanded) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                dropdownContent()
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/**
 * Setting item that opens a dialog for input.
 * Shows current value and chevron to indicate interaction.
 */
@Composable
private fun SettingDialogItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        leadingContent = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
    )
}
