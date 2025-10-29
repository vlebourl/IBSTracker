package com.tiarkaerell.ibstracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupResult
import com.tiarkaerell.ibstracker.data.model.backup.BackupSettings
import com.tiarkaerell.ibstracker.data.model.backup.RestoreResult
import com.tiarkaerell.ibstracker.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 */
class BackupViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {

    // ==================== STATE ====================

    private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    val settings = backupRepository.observeSettings()
    val localBackups = backupRepository.observeLocalBackups()

    // Cloud backups - requires access token
    // TODO: Get access token from GoogleAuthManager
    private val _cloudBackups = MutableStateFlow<List<BackupFile>>(emptyList())
    val cloudBackups: StateFlow<List<BackupFile>> = _cloudBackups.asStateFlow()

    init {
        // Initialize cloud backups observation
        viewModelScope.launch {
            backupRepository.observeCloudBackups(null).collect {  // TODO: Pass actual access token
                _cloudBackups.value = it
            }
        }
    }

    // ==================== ACTIONS ====================

    /**
     * Creates a new local backup.
     */
    fun createLocalBackup() {
        viewModelScope.launch {
            _uiState.value = BackupUiState.CreatingBackup

            val result = backupRepository.createLocalBackup()
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
     * IMPORTANT: After successful restore, the app should be restarted
     * to reload the database with restored data.
     *
     * @param backupFile The backup to restore from
     */
    fun restoreBackup(backupFile: BackupFile) {
        viewModelScope.launch {
            _uiState.value = BackupUiState.RestoringBackup

            val result = backupRepository.restoreFromBackup(backupFile)
            _uiState.value = when (result) {
                is RestoreResult.Success -> BackupUiState.RestoreCompleted(
                    message = "Restored ${result.itemsRestored} items in ${result.durationMs}ms. Please restart the app.",
                    itemsRestored = result.itemsRestored
                )
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
     */
    fun deleteBackup(backupFile: BackupFile) {
        viewModelScope.launch {
            val success = if (backupFile.location == com.tiarkaerell.ibstracker.data.model.backup.BackupLocation.LOCAL) {
                backupRepository.deleteLocalBackup(backupFile)
            } else {
                // TODO: Get access token from GoogleAuthManager
                backupRepository.deleteCloudBackup(backupFile, accessToken = null)
            }

            if (success) {
                _uiState.value = BackupUiState.BackupDeleted
                kotlinx.coroutines.delay(2000)
                _uiState.value = BackupUiState.Idle
            } else {
                _uiState.value = BackupUiState.Error("Failed to delete backup")
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
     */
    fun syncNow() {
        viewModelScope.launch {
            _uiState.value = BackupUiState.SyncingToCloud

            val result = backupRepository.syncToCloud()
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
    object BackupDeleted : BackupUiState()
    data class BackupCreated(val message: String) : BackupUiState()
    data class RestoreCompleted(val message: String, val itemsRestored: Int) : BackupUiState()
    data class CloudSyncCompleted(val message: String) : BackupUiState()
    data class Error(val message: String) : BackupUiState()
}
