package com.tiarkaerell.ibstracker.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.health.GoogleFitManager
import com.tiarkaerell.ibstracker.data.model.Language
import com.tiarkaerell.ibstracker.data.model.Units
import com.tiarkaerell.ibstracker.data.model.UserProfile
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository
import com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val context: Context,
    private val database: AppDatabase
) : ViewModel() {

    val language: StateFlow<Language> = settingsRepository.languageFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Language.ENGLISH
        )

    val units: StateFlow<Units> = settingsRepository.unitsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Units.METRIC
        )

    val userProfile: StateFlow<UserProfile> = settingsRepository.userProfileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    // Google services
    private val googleDriveBackup = GoogleDriveBackup(context, database)
    private val googleFitManager = GoogleFitManager(context)
    
    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()
    
    private val _healthPermissions = MutableStateFlow(false)
    val healthPermissions: StateFlow<Boolean> = _healthPermissions.asStateFlow()

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            settingsRepository.setLanguage(language)
        }
    }

    fun setUnits(units: Units) {
        viewModelScope.launch {
            settingsRepository.setUnits(units)
        }
    }
    
    fun updateUserProfile(userProfile: UserProfile) {
        viewModelScope.launch {
            settingsRepository.updateUserProfile(userProfile.copy(lastUpdated = System.currentTimeMillis()))
        }
    }
    
    fun createBackup() {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                val result = googleDriveBackup.createBackup()
                _backupState.value = if (result.isSuccess) {
                    BackupState.Success(result.getOrNull() ?: "Backup created successfully")
                } else {
                    BackupState.Error(result.exceptionOrNull()?.message ?: "Backup failed")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Backup failed")
            }
        }
    }
    
    fun getBackupList() {
        viewModelScope.launch {
            try {
                val result = googleDriveBackup.listBackups()
                if (result.isSuccess) {
                    _backupState.value = BackupState.BackupsLoaded(result.getOrNull() ?: emptyList())
                } else {
                    _backupState.value = BackupState.Error(result.exceptionOrNull()?.message ?: "Failed to load backups")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Failed to load backups")
            }
        }
    }
    
    fun restoreBackup(fileId: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                val result = googleDriveBackup.restoreFromBackup(fileId)
                _backupState.value = if (result.isSuccess) {
                    BackupState.Success(result.getOrNull() ?: "Backup restored successfully")
                } else {
                    BackupState.Error(result.exceptionOrNull()?.message ?: "Restore failed")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Restore failed")
            }
        }
    }
    
    fun requestHealthPermissions() {
        viewModelScope.launch {
            try {
                val hasPermissions = googleFitManager.requestPermissions()
                _healthPermissions.value = hasPermissions
            } catch (e: Exception) {
                _healthPermissions.value = false
            }
        }
    }
    
    fun clearBackupState() {
        _backupState.value = BackupState.Idle
    }

    sealed class BackupState {
        object Idle : BackupState()
        object Loading : BackupState()
        data class Success(val message: String) : BackupState()
        data class Error(val message: String) : BackupState()
        data class BackupsLoaded(val backups: List<GoogleDriveBackup.DriveFile>) : BackupState()
    }
}
