package com.tiarkaerell.ibstracker.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.auth.AuthorizationManager
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import com.tiarkaerell.ibstracker.data.model.backup.BackupSettings
import com.tiarkaerell.ibstracker.data.model.backup.RestoreResult
import com.tiarkaerell.ibstracker.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Backup & Restore UI.
 *
 * Manages:
 * - Backup settings (local/cloud toggles, timestamps)
 * - Local backup list
 * - Backup creation operations
 * - Restore operations
 * - UI state (loading, errors, success messages)
 *
 * @param backupRepository Repository for backup operations
 * @param authorizationManager Manager for Google authorization and access tokens
 */
class BackupViewModel(
    private val backupRepository: BackupRepository,
    private val authorizationManager: AuthorizationManager
) : ViewModel() {

    // ==================== STATE ====================

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    val settings = backupRepository.observeSettings()

    // Local backups
    private val _localBackups = MutableStateFlow<List<BackupFile>>(emptyList())
    val localBackups: StateFlow<List<BackupFile>> = _localBackups.asStateFlow()

    // Cloud backups
    private val _cloudBackups = MutableStateFlow<List<BackupFile>>(emptyList())
    val cloudBackups: StateFlow<List<BackupFile>> = _cloudBackups.asStateFlow()

    // Combined backups (local + cloud, sorted by timestamp descending)
    val allBackups: StateFlow<List<BackupFile>> = kotlinx.coroutines.flow.combine(
        _localBackups,
        _cloudBackups
    ) { local, cloud ->
        (local + cloud).sortedByDescending { it.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initialize local backups observation
        viewModelScope.launch {
            backupRepository.observeLocalBackups().collect {
                _localBackups.value = it
            }
        }
    }

    /**
     * Refresh cloud backups list with access token.
     * Call this when user signs in or navigates to backup screen.
     *
     * @param activity Activity context required for authorization
     */
    fun refreshCloudBackups(activity: Activity) {
        viewModelScope.launch {
            val accessToken = authorizationManager.getAccessToken(activity)
            if (accessToken != null) {
                backupRepository.observeCloudBackups(accessToken).collect {
                    _cloudBackups.value = it
                }
            }
        }
    }

    // ==================== ACTIONS ====================

    /**
     * Creates a new local backup.
     * Manual backups are not overwritten (timestamped filenames).
     */
    fun createLocalBackup() {
        viewModelScope.launch {
            _uiState.value = BackupUiState.CreatingBackup

            val result = backupRepository.createLocalBackup(isAutoBackup = false)
            _uiState.value = when (result) {
                is BackupResult.Success -> BackupUiState.BackupCreated(
                    message = "Backup created in ${result.durationMs}ms"
                )
                is BackupResult.Failure -> BackupUiState.Error(
                    message = "Backup failed: ${result.message}"
                )
            }

            // Reset to idle after 3 seconds
            kotlinx.coroutines.delay(3000)
            _uiState.value = BackupUiState.Idle
        }
    }

    /**
     * Restores database from a backup file.
     *
     * For SQLite backups, the app should be restarted to reload the database.
     * For JSON backups, data is inserted directly and no restart is needed.
     *
     * @param backupFile The backup to restore from
     */
    fun restoreBackup(backupFile: BackupFile) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.RestoringBackup

            val result = backupRepository.restoreFromBackup(backupFile)
            _uiState.value = when (result) {
                is RestoreResult.Success -> {
                    val message = if (result.requiresRestart) {
                        "Restored ${result.itemsRestored} items in ${result.durationMs}ms. Please restart the app."
                    } else {
                        "Restored ${result.itemsRestored} items in ${result.durationMs}ms."
                    }
                    BackupUiState.RestoreCompleted(
                        message = message,
                        itemsRestored = result.itemsRestored,
                        requiresRestart = result.requiresRestart
                    )
                }
                is RestoreResult.Failure -> BackupUiState.Error(
                    message = "Restore failed: ${result.message}"
                )
            }
        }
    }

    /**
     * Deletes a backup file (local or cloud).
     *
     * @param backupFile The backup to delete
     * @param activity Activity context required for cloud backup deletion (to get access token)
     */
    fun deleteBackup(backupFile: BackupFile, activity: Activity?) {
        viewModelScope.launch {
            val success = if (backupFile.location == com.tiarkaerell.ibstracker.data.model.backup.BackupLocation.LOCAL) {
                backupRepository.deleteLocalBackup(backupFile)
            } else {
                // Get access token for cloud backup deletion
                val accessToken = activity?.let { authorizationManager.getAccessToken(it) }
                if (accessToken == null) {
                    _uiState.value = BackupUiState.Error("Failed to delete cloud backup: Not authorized")
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = BackupUiState.Idle
                    return@launch
                }
                backupRepository.deleteCloudBackup(backupFile, accessToken)
            }

            if (success) {
                // Refresh backup lists immediately after deletion
                refreshLocalBackups()
                activity?.let { refreshCloudBackups(it) }
                // No popup needed - file just disappears from list
                _uiState.value = BackupUiState.Idle
            } else {
                _uiState.value = BackupUiState.Error("Failed to delete backup")
                kotlinx.coroutines.delay(2000)
                _uiState.value = BackupUiState.Idle
            }
        }
    }

    /**
     * Toggles local backups on/off.
     *
     * @param enabled true to enable, false to disable
     */
    fun toggleLocalBackups(enabled: Boolean) {
        viewModelScope.launch {
            backupRepository.toggleLocalBackups(enabled)
        }
    }

    /**
     * Toggles cloud sync on/off.
     *
     * @param enabled true to enable, false to disable
     */
    fun toggleCloudSync(enabled: Boolean) {
        viewModelScope.launch {
            backupRepository.toggleCloudSync(enabled)
        }
    }

    /**
     * Manually triggers a cloud sync now (instead of waiting for scheduled 2AM sync).
     *
     * @param activity Activity context required for Google authorization
     */
    fun syncNow(activity: Activity) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.SyncingToCloud

            // Get access token from AuthorizationManager
            val accessToken = authorizationManager.getAccessToken(activity)

            if (accessToken == null) {
                _uiState.value = BackupUiState.Error(
                    message = "Cloud sync failed: Not authorized. Please sign in to Google Drive."
                )
                kotlinx.coroutines.delay(3000)
                _uiState.value = BackupUiState.Idle
                return@launch
            }

            val result = backupRepository.syncToCloud(accessToken)
            _uiState.value = when (result) {
                is BackupResult.Success -> BackupUiState.CloudSyncCompleted(
                    message = "Cloud sync completed successfully"
                )
                is BackupResult.Failure -> BackupUiState.Error(
                    message = "Cloud sync failed: ${result.message}"
                )
            }

            // Reset to idle after 3 seconds
            kotlinx.coroutines.delay(3000)
            _uiState.value = BackupUiState.Idle
        }
    }

    /**
     * Checks if a backup is compatible with current database version.
     *
     * @param backupFile The backup to check
     * @return true if compatible, false otherwise
     */
    fun isBackupCompatible(backupFile: BackupFile): Boolean {
        return backupRepository.isBackupCompatible(backupFile)
    }

    /**
     * Imports a custom JSON backup file and adds it to the backups list.
     *
     * @param context Android context for file operations
     * @param uri Uri of the JSON file to import
     */
    fun importCustomBackup(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.ImportingBackup

            try {
                val result = backupRepository.importCustomBackup(context, uri)
                when (result) {
                    is BackupResult.Success -> {
                        // Refresh backup list immediately after import
                        refreshLocalBackups()
                        // Go directly to idle - no popup needed since file appears in list
                        _uiState.value = BackupUiState.Idle
                    }
                    is BackupResult.Failure -> {
                        _uiState.value = BackupUiState.Error(
                            message = "Import failed: ${result.message}"
                        )
                        // Reset to idle after 3 seconds for error messages
                        kotlinx.coroutines.delay(3000)
                        _uiState.value = BackupUiState.Idle
                    }
                }
            } catch (e: Exception) {
                _uiState.value = BackupUiState.Error(
                    message = "Import failed: ${e.message}"
                )
                // Reset to idle after 3 seconds for error messages
                kotlinx.coroutines.delay(3000)
                _uiState.value = BackupUiState.Idle
            }
        }
    }

    /**
     * Manually refreshes the local backups list.
     * Useful after import or delete operations.
     */
    private suspend fun refreshLocalBackups() {
        // Get the latest list and update the state
        backupRepository.observeLocalBackups().first().let {
            _localBackups.value = it
        }
    }

    /**
     * Dismisses the current UI state message.
     */
    fun dismissMessage() {
        _uiState.value = BackupUiState.Idle
    }
}

/**
 * UI state for backup operations.
 */
sealed class BackupUiState {
    object Idle : BackupUiState()
    object CreatingBackup : BackupUiState()
    object RestoringBackup : BackupUiState()
    object SyncingToCloud : BackupUiState()
    object ImportingBackup : BackupUiState()
    object BackupDeleted : BackupUiState()
    data class BackupCreated(val message: String) : BackupUiState()
    data class BackupImported(val message: String) : BackupUiState()
    data class RestoreCompleted(
        val message: String,
        val itemsRestored: Int,
        val requiresRestart: Boolean = true
    ) : BackupUiState()
    data class CloudSyncCompleted(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}
