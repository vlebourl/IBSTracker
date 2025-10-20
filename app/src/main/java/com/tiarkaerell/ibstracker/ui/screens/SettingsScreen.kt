package com.tiarkaerell.ibstracker.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.auth.GoogleAuthManager
import com.tiarkaerell.ibstracker.data.auth.rememberGoogleAuthManager
import com.tiarkaerell.ibstracker.data.model.*
import com.tiarkaerell.ibstracker.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    val language by settingsViewModel.language.collectAsState()
    val units by settingsViewModel.units.collectAsState()
    val userProfile by settingsViewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    
    val googleAuthManager = rememberGoogleAuthManager()
    val authState by googleAuthManager.authState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            googleAuthManager.handleSignInResult(result.data)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Language Selection
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var languageExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = languageExpanded,
            onExpandedChange = { languageExpanded = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            OutlinedTextField(
                value = stringResource(language.displayNameRes),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = languageExpanded,
                onDismissRequest = { languageExpanded = false }
            ) {
                Language.entries.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(stringResource(lang.displayNameRes)) },
                        onClick = {
                            settingsViewModel.setLanguage(lang)
                            languageExpanded = false
                            // Recreate activity to apply new language
                            activity?.recreate()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        // Units Selection
        Text(
            text = stringResource(R.string.settings_units),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        var unitsExpanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(
            expanded = unitsExpanded,
            onExpandedChange = { unitsExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = stringResource(units.displayNameRes),
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitsExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

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

        Spacer(modifier = Modifier.height(24.dp))

        // Current selections info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_current),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_language_current, stringResource(language.displayNameRes)),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.settings_units_current, stringResource(units.displayNameRes)),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Personal Information Section
        PersonalInfoSection(
            userProfile = userProfile,
            units = units,
            onUpdateProfile = { updatedProfile ->
                settingsViewModel.updateUserProfile(updatedProfile)
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Google Services Section
        Text(
            text = stringResource(R.string.google_services_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Google Sign-In
        GoogleSignInCard(
            authState = authState,
            onSignIn = {
                coroutineScope.launch {
                    val result = googleAuthManager.signIn()
                    if (result.isFailure) {
                        signInLauncher.launch(googleAuthManager.getSignInIntent())
                    }
                }
            },
            onSignOut = {
                coroutineScope.launch {
                    googleAuthManager.signOut()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Drive Backup
        val backupState by settingsViewModel.backupState.collectAsState()
        GoogleDriveBackupCard(
            isSignedIn = authState is GoogleAuthManager.AuthState.SignedIn,
            backupState = backupState,
            onCreateBackup = {
                settingsViewModel.createBackup()
            },
            onRestoreBackup = {
                settingsViewModel.getBackupList()
            },
            onClearState = {
                settingsViewModel.clearBackupState()
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Google Fit Integration
        val healthPermissions by settingsViewModel.healthPermissions.collectAsState()
        GoogleFitIntegrationCard(
            isSignedIn = authState is GoogleAuthManager.AuthState.SignedIn,
            hasPermissions = healthPermissions,
            onConnect = {
                settingsViewModel.requestHealthPermissions()
            },
            onDisconnect = {
                // TODO: Implement Google Fit disconnection
            }
        )
    }
}

@Composable
fun GoogleSignInCard(
    authState: GoogleAuthManager.AuthState,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.google_sign_in),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.google_sign_in_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                when (authState) {
                    is GoogleAuthManager.AuthState.NotSignedIn -> {
                        Button(onClick = onSignIn) {
                            Text(stringResource(R.string.sign_in_button))
                        }
                    }
                    is GoogleAuthManager.AuthState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                    is GoogleAuthManager.AuthState.SignedIn -> {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = stringResource(R.string.signed_in_as, authState.account.email ?: ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(onClick = onSignOut) {
                                Text(stringResource(R.string.sign_out_button))
                            }
                        }
                    }
                    is GoogleAuthManager.AuthState.Error -> {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = authState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = onSignIn) {
                                Text(stringResource(R.string.sign_in_button))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleDriveBackupCard(
    isSignedIn: Boolean,
    backupState: SettingsViewModel.BackupState,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onClearState: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.backup_sync_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.google_drive_backup),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.google_drive_backup_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCreateBackup,
                    enabled = isSignedIn && backupState !is SettingsViewModel.BackupState.Loading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (backupState is SettingsViewModel.BackupState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text(stringResource(R.string.create_backup))
                    }
                }
                OutlinedButton(
                    onClick = onRestoreBackup,
                    enabled = isSignedIn && backupState !is SettingsViewModel.BackupState.Loading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.restore_backup))
                }
            }
            
            // Display backup state messages
            when (backupState) {
                is SettingsViewModel.BackupState.Success -> {
                    Text(
                        text = backupState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LaunchedEffect(backupState) {
                        kotlinx.coroutines.delay(3000)
                        onClearState()
                    }
                }
                is SettingsViewModel.BackupState.Error -> {
                    Text(
                        text = backupState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LaunchedEffect(backupState) {
                        kotlinx.coroutines.delay(5000)
                        onClearState()
                    }
                }
                is SettingsViewModel.BackupState.BackupsLoaded -> {
                    Text(
                        text = "Found ${backupState.backups.size} backups",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    // TODO: Show backup list dialog
                }
                else -> {
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
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    
    Text(
        text = stringResource(R.string.personal_info_title),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 16.dp)
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Date of Birth
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.date_of_birth_label),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { showDatePicker = true }) {
                    Text(
                        userProfile.dateOfBirth?.let { 
                            dateFormat.format(Date(it)) 
                        } ?: stringResource(R.string.not_specified)
                    )
                }
            }
            
            Divider()
            
            // Sex
            var sexExpanded by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sex_label),
                    style = MaterialTheme.typography.titleMedium
                )
                
                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = it }
                ) {
                    TextButton(
                        onClick = { sexExpanded = true },
                        modifier = Modifier.menuAnchor()
                    ) {
                        Text(stringResource(userProfile.sex.displayNameRes))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    
                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        Sex.entries.forEach { sex ->
                            DropdownMenuItem(
                                text = { Text(stringResource(sex.displayNameRes)) },
                                onClick = {
                                    onUpdateProfile(userProfile.copy(sex = sex))
                                    sexExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Divider()
            
            // Height
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.height_label),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { showHeightDialog = true }) {
                    Text(
                        userProfile.heightCm?.let { height ->
                            if (units == Units.METRIC) {
                                stringResource(R.string.height_cm_format, height)
                            } else {
                                val (feet, inches) = userProfile.getHeightInFeetInches() ?: Pair(0, 0)
                                stringResource(R.string.height_ft_in_format, feet, inches)
                            }
                        } ?: stringResource(R.string.not_specified)
                    )
                }
            }
            
            Divider()
            
            // Weight
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weight_label),
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { showWeightDialog = true }) {
                    Text(
                        userProfile.weightKg?.let { weight ->
                            if (units == Units.METRIC) {
                                stringResource(R.string.weight_kg_format, weight)
                            } else {
                                val pounds = userProfile.getWeightInPounds() ?: 0
                                stringResource(R.string.weight_lb_format, pounds)
                            }
                        } ?: stringResource(R.string.not_specified)
                    )
                }
            }
            
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
