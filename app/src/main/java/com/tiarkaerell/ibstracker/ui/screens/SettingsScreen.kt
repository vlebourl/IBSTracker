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
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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
                            languageExpanded = false
                            // Save language and wait for it to complete before recreating activity
                            coroutineScope.launch {
                                settingsViewModel.setLanguage(lang)
                                // Small delay to ensure DataStore write completes
                                kotlinx.coroutines.delay(100)
                                // Recreate activity to apply new language
                                activity?.recreate()
                            }
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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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
                    googleAuthManager.signIn()
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
            onRestoreWithStrategy = { fileId, strategy ->
                settingsViewModel.restoreBackup(fileId, strategy)
            },
            onRestoreWithPassword = { fileId, strategy, password ->
                settingsViewModel.restoreBackup(fileId, strategy, password)
            },
            onClearState = {
                settingsViewModel.clearBackupState()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Backup Password Card
        val backupPassword by settingsViewModel.backupPassword.collectAsState()
        BackupPasswordCard(
            hasPassword = settingsViewModel.hasBackupPassword(),
            currentPassword = backupPassword,
            onPasswordChange = { password ->
                settingsViewModel.setBackupPassword(password)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Version footer
        Text(
            text = "Version ${context.packageManager.getPackageInfo(context.packageName, 0).versionName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                                text = stringResource(R.string.signed_in_as, authState.email),
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
    onRestoreWithStrategy: (String, com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.MergeStrategy) -> Unit,
    onRestoreWithPassword: (String, com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.MergeStrategy, String) -> Unit,
    onClearState: () -> Unit
) {
    var showBackupListDialog by remember { mutableStateOf(false) }
    var selectedBackupId by remember { mutableStateOf<String?>(null) }
    var showMergeStrategyDialog by remember { mutableStateOf(false) }
    var showRestorePasswordDialog by remember { mutableStateOf(false) }
    var restoreFileId by remember { mutableStateOf<String?>(null) }
    var restoreMergeStrategy by remember { mutableStateOf<com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.MergeStrategy?>(null) }
    var passwordError by remember { mutableStateOf(false) }

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
                    LaunchedEffect(backupState) {
                        showBackupListDialog = true
                    }
                }
                is SettingsViewModel.BackupState.PasswordRequired -> {
                    LaunchedEffect(backupState) {
                        restoreFileId = backupState.fileId
                        restoreMergeStrategy = backupState.mergeStrategy
                        passwordError = false
                        showRestorePasswordDialog = true
                    }
                }
                is SettingsViewModel.BackupState.PasswordIncorrect -> {
                    LaunchedEffect(backupState) {
                        restoreFileId = backupState.fileId
                        restoreMergeStrategy = backupState.mergeStrategy
                        passwordError = true
                        showRestorePasswordDialog = true
                    }
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

    // Backup List Dialog
    if (showBackupListDialog && backupState is SettingsViewModel.BackupState.BackupsLoaded) {
        BackupListDialog(
            backups = backupState.backups,
            onBackupSelected = { backupId ->
                selectedBackupId = backupId
                showBackupListDialog = false
                showMergeStrategyDialog = true
            },
            onDismiss = {
                showBackupListDialog = false
                onClearState()
            }
        )
    }

    // Merge Strategy Dialog
    if (showMergeStrategyDialog && selectedBackupId != null) {
        MergeStrategyDialog(
            onStrategySelected = { strategy ->
                showMergeStrategyDialog = false
                onRestoreWithStrategy(selectedBackupId!!, strategy)
                selectedBackupId = null
            },
            onDismiss = {
                showMergeStrategyDialog = false
                selectedBackupId = null
            }
        )
    }

    // Restore Password Dialog
    if (showRestorePasswordDialog && restoreFileId != null && restoreMergeStrategy != null) {
        RestorePasswordDialog(
            hasError = passwordError,
            onPasswordProvided = { password ->
                showRestorePasswordDialog = false
                onRestoreWithPassword(restoreFileId!!, restoreMergeStrategy!!, password)
                restoreFileId = null
                restoreMergeStrategy = null
                passwordError = false
            },
            onDismiss = {
                showRestorePasswordDialog = false
                restoreFileId = null
                restoreMergeStrategy = null
                passwordError = false
                onClearState()
            }
        )
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
            
            HorizontalDivider()
            
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
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
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
            
            HorizontalDivider()
            
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
            
            HorizontalDivider()
            
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupListDialog(
    backups: List<com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.DriveFile>,
    onBackupSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.select_backup))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (backups.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_backups_found),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    backups.forEach { backup ->
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                        val backupDate = dateFormat.format(Date(backup.createdTime))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = { onBackupSelected(backup.id) }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = backup.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = backupDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.backup_size_kb, backup.size / 1024),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MergeStrategyDialog(
    onStrategySelected: (com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.MergeStrategy) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.restore_strategy_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.restore_strategy_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        onStrategySelected(com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.MergeStrategy.KEEP_LOCAL)
                        onDismiss()
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.merge_keep_local),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.merge_keep_local_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        onStrategySelected(com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.MergeStrategy.KEEP_BACKUP)
                        onDismiss()
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.merge_keep_backup),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.merge_keep_backup_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { 
                        onStrategySelected(com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup.MergeStrategy.KEEP_BOTH)
                        onDismiss()
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.merge_keep_both),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.merge_keep_both_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}

@Composable
fun BackupPasswordCard(
    hasPassword: Boolean,
    currentPassword: String,
    onPasswordChange: (String) -> Unit
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.backup_password_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.backup_password_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = hasPassword,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            showPasswordDialog = true
                        } else {
                            showDisableDialog = true
                        }
                    }
                )
            }

            if (hasPassword) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.backup_password_enabled),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.backup_password_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    // Password setup dialog
    if (showPasswordDialog) {
        PasswordSetupDialog(
            onDismiss = { showPasswordDialog = false },
            onPasswordSet = { newPassword ->
                onPasswordChange(newPassword)
                showPasswordDialog = false
            }
        )
    }

    // Disable password verification dialog
    if (showDisableDialog) {
        PasswordVerifyDialog(
            currentPassword = currentPassword,
            onDismiss = { showDisableDialog = false },
            onPasswordVerified = {
                onPasswordChange("")
                showDisableDialog = false
            }
        )
    }
}

@Composable
fun PasswordSetupDialog(
    onDismiss: () -> Unit,
    onPasswordSet: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.setup_password_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.setup_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.password_label)) },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                        autoCorrectEnabled = false
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.confirm_password_label)) },
                    visualTransformation = if (confirmPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                        autoCorrectEnabled = false
                    ),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.password_mismatch_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Warning message
                Text(
                    text = stringResource(R.string.backup_password_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.isEmpty()) {
                        showError = true
                    } else if (password != confirmPassword) {
                        showError = true
                    } else {
                        onPasswordSet(password)
                    }
                }
            ) {
                Text(stringResource(R.string.button_enable))
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
fun PasswordVerifyDialog(
    currentPassword: String,
    onDismiss: () -> Unit,
    onPasswordVerified: () -> Unit
) {
    var enteredPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.verify_password_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.verify_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = enteredPassword,
                    onValueChange = {
                        enteredPassword = it
                        showError = false
                    },
                    label = { Text(stringResource(R.string.password_label)) },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                        autoCorrectEnabled = false
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError
                )

                if (showError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.password_incorrect_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.disable_password_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (enteredPassword == currentPassword) {
                        onPasswordVerified()
                    } else {
                        showError = true
                    }
                }
            ) {
                Text(stringResource(R.string.button_disable))
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
fun RestorePasswordDialog(
    hasError: Boolean,
    onPasswordProvided: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.restore_password_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.restore_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password_label)) },
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password,
                        autoCorrectEnabled = false
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = hasError
                )

                if (hasError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.password_incorrect_error),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (password.isNotEmpty()) {
                        onPasswordProvided(password)
                    }
                }
            ) {
                Text(stringResource(R.string.button_restore))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.button_cancel))
            }
        }
    )
}
