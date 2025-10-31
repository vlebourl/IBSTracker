package com.tiarkaerell.ibstracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tiarkaerell.ibstracker.data.auth.rememberGoogleAuthManager
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.toMetadata
import com.tiarkaerell.ibstracker.ui.components.BackupListItem
import com.tiarkaerell.ibstracker.ui.components.BackupStatusCard
import com.tiarkaerell.ibstracker.ui.viewmodel.BackupUiState
import com.tiarkaerell.ibstracker.ui.viewmodel.BackupViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backup & Restore Settings Screen.
 *
 * Features:
 * - Backup status overview card
 * - Toggle switches for local/cloud backups
 * - List of available backups
 * - Manual backup creation
 * - Restore functionality with confirmation dialog
 * - Delete backup functionality
 *
 * @param viewModel BackupViewModel for backup operations
 * @param onNavigateBack Callback when back button clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    viewModel: BackupViewModel,
    onNavigateBack: () -> Unit,
    hasBackupPassword: Boolean = false,
    backupPassword: String = "",
    onPasswordChange: (String) -> Unit = {}
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle(
        initialValue = com.tiarkaerell.ibstracker.data.model.backup.BackupSettings()
    )
    val allBackups by viewModel.allBackups.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Google Auth Manager
    val googleAuthManager = rememberGoogleAuthManager()
    val authState by googleAuthManager.authState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    // State for restore confirmation dialog
    var backupToRestore by remember { mutableStateOf<BackupFile?>(null) }
    var backupToDelete by remember { mutableStateOf<BackupFile?>(null) }
    var importedBackupUri by remember { mutableStateOf<Uri?>(null) }

    // Import launcher for JSON files
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { importedBackupUri = it }
    }

    // Refresh cloud backups when screen loads
    LaunchedEffect(Unit) {
        activity?.let { viewModel.refreshCloudBackups(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.createLocalBackup() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Create backup"
                    )
                },
                text = { Text("Backup Now") },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for FAB
        ) {
            // Status Card
            item {
                BackupStatusCard(settings = settings)
            }

            // Backup Password Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                BackupPasswordCard(
                    hasPassword = hasBackupPassword,
                    currentPassword = backupPassword,
                    onPasswordChange = onPasswordChange
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Settings toggles
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "Settings") {
                    SettingSwitchItem(
                        label = "Local Backups",
                        description = "Automatically backup after every change",
                        checked = settings.localBackupsEnabled,
                        onCheckedChange = { viewModel.toggleLocalBackups(it) },
                        icon = Icons.Default.Save
                    )

                    SettingSwitchItem(
                        label = "Cloud Sync",
                        description = "Daily sync to Google Drive at 2:00 AM",
                        checked = settings.cloudSyncEnabled,
                        onCheckedChange = { viewModel.toggleCloudSync(it, context) },
                        enabled = settings.isGoogleSignedIn,
                        icon = Icons.Default.CloudUpload
                    )

                    if (!settings.isGoogleSignedIn && settings.cloudSyncEnabled) {
                        Text(
                            text = "Sign in to Google to enable cloud sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Google Account section
            item {
                GoogleAccountSection(
                    authState = authState,
                    settings = settings,
                    uiState = uiState,
                    onSignInClick = {
                        coroutineScope.launch {
                            googleAuthManager.signIn()
                        }
                    },
                    onSignOutClick = {
                        coroutineScope.launch {
                            googleAuthManager.signOut()
                        }
                    },
                    onSyncNowClick = {
                        activity?.let { viewModel.syncNow(it) }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Backups list
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Backups (${allBackups.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalIconButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Import backup"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (allBackups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "No backups available yet.\nTap \"Backup Now\" to create your first backup.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(allBackups) { backup ->
                    val isCompatible = viewModel.isBackupCompatible(backup)

                    BackupListItem(
                        backupFile = backup,
                        isCompatible = isCompatible,
                        onRestoreClick = { backupToRestore = backup },
                        onDeleteClick = { backupToDelete = backup }
                    )
                }
            }
        }

        // Restore confirmation dialog
        if (backupToRestore != null) {
            RestoreConfirmationDialog(
                backupFile = backupToRestore!!,
                onConfirm = {
                    viewModel.restoreBackup(backupToRestore!!)
                    backupToRestore = null
                },
                onDismiss = { backupToRestore = null }
            )
        }

        // Delete confirmation dialog
        if (backupToDelete != null) {
            DeleteConfirmationDialog(
                backupFile = backupToDelete!!,
                onConfirm = {
                    viewModel.deleteBackup(backupToDelete!!, activity)
                    backupToDelete = null
                },
                onDismiss = { backupToDelete = null }
            )
        }

        // Import confirmation dialog
        if (importedBackupUri != null) {
            ImportConfirmationDialog(
                onConfirm = {
                    coroutineScope.launch {
                        viewModel.importCustomBackup(context, importedBackupUri!!)
                        importedBackupUri = null
                    }
                },
                onDismiss = { importedBackupUri = null }
            )
        }

        // Show snackbar for UI state messages
        when (val state = uiState) {
            is BackupUiState.BackupCreated -> {
                LaunchedEffect(state) {
                    // Snackbar would be shown here in production
                }
            }
            is BackupUiState.BackupImported -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissMessage() },
                    title = { Text("Backup Imported") },
                    text = { Text("${state.message}\n\nThe backup has been added to your available backups list. You can restore it whenever you want.") },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissMessage() }) {
                            Text("OK")
                        }
                    }
                )
            }
            is BackupUiState.RestoreCompleted -> {
                val context = LocalContext.current
                AlertDialog(
                    onDismissRequest = { viewModel.dismissMessage() },
                    title = { Text("Restore Complete") },
                    text = {
                        Text(
                            "${state.message}\n\nYour data has been restored successfully!" +
                            if (state.requiresRestart) "\n\nPlease restart the app to see the restored data." else ""
                        )
                    },
                    confirmButton = {
                        if (state.requiresRestart) {
                            TextButton(
                                onClick = {
                                    // Force restart the app
                                    val packageManager = context.packageManager
                                    val intent = packageManager.getLaunchIntentForPackage(context.packageName)
                                    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    intent?.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    kotlin.system.exitProcess(0)
                                }
                            ) {
                                Text("Restart App")
                            }
                        } else {
                            TextButton(onClick = { viewModel.dismissMessage() }) {
                                Text("OK")
                            }
                        }
                    },
                    dismissButton = if (state.requiresRestart) {
                        {
                            TextButton(onClick = { viewModel.dismissMessage() }) {
                                Text("Later")
                            }
                        }
                    } else null
                )
            }
            is BackupUiState.Error -> {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissMessage() },
                    title = { Text("Error") },
                    text = { Text(state.message) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissMessage() }) {
                            Text("OK")
                        }
                    }
                )
            }
            else -> { /* No message to show */ }
        }
    }
}

/**
 * Reusable section header for grouping related settings.
 * Matches the Material Design 3 pattern from main SettingsScreen.
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
 * Setting item with a switch toggle.
 * Uses Material Design 3 ListItem for consistency.
 */
@Composable
private fun SettingSwitchItem(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    ListItem(
        headlineContent = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RestoreConfirmationDialog(
    backupFile: BackupFile,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isJsonBackup = backupFile.fileName.endsWith(".json")

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null
            )
        },
        title = { Text("Restore from Backup?") },
        text = {
            Column {
                Text(
                    if (isJsonBackup) {
                        "This will add all data from the backup to your current data:"
                    } else {
                        "This will replace all current data with the backup from:"
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = backupFile.toMetadata().humanReadableDate,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!isJsonBackup) {
                    Text("A safety backup will be created before restoring.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You will need to restart the app after restore.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(
                        text = "Data will be added immediately without restart.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    backupFile: BackupFile,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Backup?") },
        text = {
            Text("Are you sure you want to delete the backup from ${backupFile.toMetadata().humanReadableDate}? This cannot be undone.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Google Account Section - Sign in/out and cloud sync controls.
 *
 * @param authState Current Google authentication state
 * @param settings Backup settings with sync timestamps
 * @param onSignInClick Callback when sign in button clicked
 * @param onSignOutClick Callback when sign out button clicked
 * @param onSyncNowClick Callback when manual sync button clicked
 * @param modifier Modifier for this section
 */
@Composable
private fun GoogleAccountSection(
    authState: com.tiarkaerell.ibstracker.data.auth.GoogleAuthManager.AuthState,
    settings: com.tiarkaerell.ibstracker.data.model.backup.BackupSettings,
    uiState: BackupUiState,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onSyncNowClick: () -> Unit
) {
    SettingsSection(title = "Google Account") {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            when (authState) {
            is com.tiarkaerell.ibstracker.data.auth.GoogleAuthManager.AuthState.NotSignedIn -> {
                // Not signed in - show sign-in button
                Column {
                    Text(
                        text = "Sign in with Google to enable cloud backup",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Sign in with Google")
                    }
                }
            }
            is com.tiarkaerell.ibstracker.data.auth.GoogleAuthManager.AuthState.Loading -> {
                // Signing in - show progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Signing in...")
                }
            }
            is com.tiarkaerell.ibstracker.data.auth.GoogleAuthManager.AuthState.SignedIn -> {
                // Signed in - show account info and sync controls
                Column {
                    // Account info row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authState.email,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Signed in",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(onClick = onSignOutClick) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Sign out")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Last sync timestamp
                    if (settings.lastCloudSyncTimestamp != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Last cloud sync",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatTimestamp(settings.lastCloudSyncTimestamp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Text(
                            text = "No cloud sync yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Infinite rotation animation for sync icon
                    val infiniteTransition = rememberInfiniteTransition(label = "sync rotation")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    // Manual sync button
                    Button(
                        onClick = onSyncNowClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = settings.cloudSyncEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .rotate(if (uiState is BackupUiState.SyncingToCloud) rotation else 0f)
                        )
                        Text("Sync Now")
                    }

                    if (!settings.cloudSyncEnabled) {
                        Text(
                            text = "Enable cloud sync above to use manual sync",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            is com.tiarkaerell.ibstracker.data.auth.GoogleAuthManager.AuthState.Error -> {
                // Error state - show error message
                Column {
                    Text(
                        text = authState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Try Again")
                    }
                }
            }
        }
        }
    }
}

/**
 * Import Confirmation Dialog - Confirm import of custom JSON backup.
 *
 * @param onConfirm Callback when user confirms import
 * @param onDismiss Callback when user dismisses dialog
 */
@Composable
private fun ImportConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.FileUpload,
                contentDescription = null
            )
        },
        title = { Text("Import Backup?") },
        text = {
            Column {
                Text("This will import a custom JSON backup file into your backups list.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("The file will be validated and added to your available backups.")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You can then restore it like any other backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.backup_password_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                    verticalAlignment = Alignment.CenterVertically
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

/**
 * Formats a timestamp (milliseconds since epoch) to a human-readable string.
 */
private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
