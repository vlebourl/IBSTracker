package com.tiarkaerell.ibstracker.ui.viewmodel

import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.Language
import com.tiarkaerell.ibstracker.data.model.Units
import com.tiarkaerell.ibstracker.data.model.UserProfile
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository
import com.tiarkaerell.ibstracker.data.sync.GoogleDriveBackup
import com.tiarkaerell.ibstracker.data.sync.JsonExportImport
import com.tiarkaerell.ibstracker.data.sync.PasswordRequiredException
import com.tiarkaerell.ibstracker.data.sync.IncorrectPasswordException
import java.io.File
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    private val googleDriveBackup = GoogleDriveBackup(context, database, settingsRepository)

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    private val _healthPermissions = MutableStateFlow(false)
    val healthPermissions: StateFlow<Boolean> = _healthPermissions.asStateFlow()

    private val _backupPassword = MutableStateFlow("")
    val backupPassword: StateFlow<String> = _backupPassword.asStateFlow()

    // Authorization events - emit when Activity is needed for authorization UI
    private val _authorizationEvents = MutableSharedFlow<AuthorizationEvent>()
    val authorizationEvents: SharedFlow<AuthorizationEvent> = _authorizationEvents.asSharedFlow()

    // Cached access token from authorization (will be set by UI after authorization completes)
    private var cachedAccessToken: String? = null

    // Track pending operation while waiting for authorization
    private var pendingOperation: PendingOperation? = null

    /**
     * Pending operations that are waiting for Drive authorization
     */
    private sealed class PendingOperation {
        object CreateBackup : PendingOperation()
        object GetBackupList : PendingOperation()
        data class RestoreBackup(val fileId: String, val mergeStrategy: GoogleDriveBackup.MergeStrategy, val password: String?) : PendingOperation()
        data class GetBackupMetadata(val fileId: String) : PendingOperation()
    }

    init {
        // Load current backup password
        _backupPassword.value = settingsRepository.getBackupPassword() ?: ""
    }

    fun setBackupPassword(password: String) {
        _backupPassword.value = password
        settingsRepository.setBackupPassword(password.ifEmpty { null })
    }

    fun hasBackupPassword(): Boolean {
        return settingsRepository.hasBackupPassword()
    }

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
                // Get or request authorization for Drive access
                if (cachedAccessToken == null) {
                    pendingOperation = PendingOperation.CreateBackup
                    requestDriveAuthorization()
                    // Keep loading state - operation will continue after authorization
                    return@launch
                }

                val result = googleDriveBackup.createBackup(cachedAccessToken)
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
            _backupState.value = BackupState.Loading
            try {
                // Get or request authorization for Drive access
                if (cachedAccessToken == null) {
                    pendingOperation = PendingOperation.GetBackupList
                    requestDriveAuthorization()
                    // Keep loading state - operation will continue after authorization
                    return@launch
                }

                val result = googleDriveBackup.listBackups(cachedAccessToken)
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
    
    fun restoreBackup(fileId: String, mergeStrategy: GoogleDriveBackup.MergeStrategy, password: String? = null) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                // Get or request authorization for Drive access
                if (cachedAccessToken == null) {
                    pendingOperation = PendingOperation.RestoreBackup(fileId, mergeStrategy, password)
                    requestDriveAuthorization()
                    // Keep loading state - operation will continue after authorization
                    return@launch
                }

                val result = googleDriveBackup.restoreWithMerge(fileId, mergeStrategy, password, cachedAccessToken)
                _backupState.value = if (result.isSuccess) {
                    BackupState.Success(result.getOrNull() ?: "Backup restored successfully")
                } else {
                    val exception = result.exceptionOrNull()
                    when (exception) {
                        is PasswordRequiredException -> {
                            BackupState.PasswordRequired(fileId, mergeStrategy)
                        }
                        is IncorrectPasswordException -> {
                            BackupState.PasswordIncorrect(fileId, mergeStrategy)
                        }
                        else -> {
                            BackupState.Error(exception?.message ?: "Restore failed")
                        }
                    }
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Restore failed")
            }
        }
    }

    fun getBackupMetadata(fileId: String) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            try {
                // Get or request authorization for Drive access
                if (cachedAccessToken == null) {
                    pendingOperation = PendingOperation.GetBackupMetadata(fileId)
                    requestDriveAuthorization()
                    // Keep loading state - operation will continue after authorization
                    return@launch
                }

                val result = googleDriveBackup.getBackupMetadata(fileId, cachedAccessToken)
                if (result.isSuccess) {
                    _backupState.value = BackupState.MetadataLoaded(result.getOrNull()!!)
                } else {
                    _backupState.value = BackupState.Error(result.exceptionOrNull()?.message ?: "Failed to load backup details")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "Failed to load backup details")
            }
        }
    }
    
    fun requestHealthPermissions() {
        // Google Fit integration removed - Health Connect API should be used instead
        _healthPermissions.value = false
    }
    
    fun clearBackupState() {
        _backupState.value = BackupState.Idle
    }

    /**
     * Request Google Drive authorization
     * This will trigger an authorization event that the UI should handle
     */
    fun requestDriveAuthorization() {
        viewModelScope.launch {
            _authorizationEvents.emit(AuthorizationEvent.RequestDriveAuthorization)
        }
    }

    /**
     * Handle authorization result from UI
     * Stores the access token and executes any pending operation
     */
    fun handleAuthorizationResult(accessToken: String?) {
        cachedAccessToken = accessToken

        // If authorization succeeded and there's a pending operation, execute it
        if (accessToken != null && pendingOperation != null) {
            executePendingOperation()
        } else if (accessToken == null && pendingOperation != null) {
            // Authorization failed
            _backupState.value = BackupState.Error("Authorization failed. Please try again.")
            pendingOperation = null
        }
    }

    /**
     * Execute the pending operation after authorization completes
     */
    private fun executePendingOperation() {
        val operation = pendingOperation ?: return
        pendingOperation = null  // Clear before executing to avoid loops

        when (operation) {
            is PendingOperation.CreateBackup -> createBackup()
            is PendingOperation.GetBackupList -> getBackupList()
            is PendingOperation.RestoreBackup -> restoreBackup(operation.fileId, operation.mergeStrategy, operation.password)
            is PendingOperation.GetBackupMetadata -> getBackupMetadata(operation.fileId)
        }
    }

    /**
     * Get the cached access token for Drive operations
     * Returns null if not authorized or token expired
     */
    fun getCachedAccessToken(): String? {
        return cachedAccessToken
    }

    /**
     * Export database to local JSON file
     */
    suspend fun exportToLocalFile(): File {
        return JsonExportImport.exportToJson(context, database)
    }

    /**
     * Import database from local JSON file
     */
    suspend fun importFromLocalFile(file: File, clearExisting: Boolean = false): Result<String> {
        return JsonExportImport.importFromJson(context, database, file, clearExisting)
    }

    sealed class BackupState {
        object Idle : BackupState()
        object Loading : BackupState()
        data class Success(val message: String) : BackupState()
        data class Error(val message: String) : BackupState()
        data class BackupsLoaded(val backups: List<GoogleDriveBackup.DriveFile>) : BackupState()
        data class MetadataLoaded(val metadata: GoogleDriveBackup.BackupMetadata) : BackupState()
        data class PasswordRequired(val fileId: String, val mergeStrategy: GoogleDriveBackup.MergeStrategy) : BackupState()
        data class PasswordIncorrect(val fileId: String, val mergeStrategy: GoogleDriveBackup.MergeStrategy) : BackupState()
    }

    /**
     * Authorization events that require Activity context
     * Emitted by ViewModel, collected by UI layer
     */
    sealed class AuthorizationEvent {
        /**
         * Request Google Drive authorization
         * UI should launch authorization flow with Activity
         */
        object RequestDriveAuthorization : AuthorizationEvent()
    }
}
