/**
 * API Contract: BackupRepository
 *
 * Repository layer for coordinating backup operations across local and cloud storage.
 * Exposes high-level operations for UI layer while delegating to specialized managers.
 *
 * @see data-model.md for data structures and relationships
 */
interface BackupRepository {

    // ==================== Settings ====================

    /**
     * Observes current backup settings as reactive Flow.
     *
     * Emits whenever settings change (toggles, timestamps, Google account, etc.)
     *
     * @return Flow of BackupSettings
     */
    fun observeSettings(): Flow<BackupSettings>

    /**
     * Updates a specific backup setting.
     *
     * @param settings New settings to persist
     */
    suspend fun updateSettings(settings: BackupSettings)

    /**
     * Toggles local backup functionality on/off.
     *
     * When disabled, automatic backups after data changes will not be created.
     * Manual "Backup now" will still work.
     *
     * @param enabled true to enable, false to disable
     */
    suspend fun setLocalBackupsEnabled(enabled: Boolean)

    /**
     * Toggles cloud sync functionality on/off.
     *
     * When disabled, scheduled 2:00 AM sync will not run.
     * Manual "Backup now" will still work.
     *
     * @param enabled true to enable, false to disable
     */
    suspend fun setCloudSyncEnabled(enabled: Boolean)

    // ==================== Local Backups ====================

    /**
     * Creates a local backup of the current database.
     *
     * Delegates to BackupManager.createLocalBackup().
     * Updates BackupSettings.lastLocalBackupTimestamp on success.
     *
     * @return BackupResult with created backup or error
     */
    suspend fun createLocalBackup(): BackupResult

    /**
     * Lists all local backups sorted by timestamp (newest first).
     *
     * @return Flow of local BackupFile list
     */
    fun observeLocalBackups(): Flow<List<BackupFile>>

    /**
     * Deletes a specific local backup.
     *
     * @param backupFile The backup to delete
     * @return true if deleted, false if failed
     */
    suspend fun deleteLocalBackup(backupFile: BackupFile): Boolean

    /**
     * Deletes all local backups.
     *
     * @return Number of backups deleted
     */
    suspend fun deleteAllLocalBackups(): Int

    // ==================== Cloud Backups ====================

    /**
     * Observes current cloud sync status.
     *
     * Emits whenever sync state changes (SYNCED → SYNCING → SYNCED/FAILED).
     *
     * @return Flow of SyncStatus
     */
    fun observeSyncStatus(): Flow<SyncStatus>

    /**
     * Lists all cloud backups sorted by timestamp (newest first).
     *
     * Requires Google Sign-In. Returns empty list if not authenticated.
     *
     * @return Flow of cloud BackupFile list
     */
    fun observeCloudBackups(): Flow<List<BackupFile>>

    /**
     * Triggers a manual cloud sync immediately.
     *
     * Bypasses schedule and constraints. Used for "Backup now" button.
     *
     * @return WorkRequest ID for tracking progress
     */
    suspend fun triggerManualCloudSync(): UUID

    /**
     * Deletes a specific cloud backup from Google Drive.
     *
     * Requires Google Sign-In and network connection.
     *
     * @param backupFile The cloud backup to delete
     * @return true if deleted, false if failed
     */
    suspend fun deleteCloudBackup(backupFile: BackupFile): Boolean

    // ==================== Restore ====================

    /**
     * Restores database from a backup file (local or cloud).
     *
     * Delegates to RestoreManager.restoreFromBackup().
     * Triggers UI refresh signal after successful restore.
     *
     * IMPORTANT: App should be restarted after restore.
     *
     * @param backupFile The backup to restore from
     * @return RestoreResult with count of restored items or error
     */
    suspend fun restoreFromBackup(backupFile: BackupFile): RestoreResult

    /**
     * Checks if a backup is compatible with current app version.
     *
     * @param backupFile The backup to check
     * @return true if compatible, false if version mismatch
     */
    suspend fun isBackupCompatible(backupFile: BackupFile): Boolean

    // ==================== Google Sign-In ====================

    /**
     * Signs in to Google with Drive access.
     *
     * Required scope: DriveScopes.DRIVE_APPDATA
     *
     * @param activity Activity context for Sign-In UI
     * @return true if sign-in successful, false if failed/canceled
     */
    suspend fun signInToGoogle(activity: Activity): Boolean

    /**
     * Signs out of Google and disables cloud sync.
     *
     * Cancels all scheduled cloud backups.
     *
     * @return true if sign-out successful
     */
    suspend fun signOutOfGoogle(): Boolean

    /**
     * Checks if user is currently signed in to Google.
     *
     * @return true if authenticated with Drive access, false otherwise
     */
    suspend fun isGoogleSignedIn(): Boolean

    // ==================== Storage Usage ====================

    /**
     * Calculates total storage usage (local + cloud).
     *
     * @return Pair of (local usage bytes, cloud usage bytes)
     */
    suspend fun calculateStorageUsage(): Pair<Long, Long>

    /**
     * Observes storage usage metrics.
     *
     * Emits whenever backups are created/deleted.
     *
     * @return Flow of (local bytes, cloud bytes, backup count)
     */
    fun observeStorageUsage(): Flow<Triple<Long, Long, Int>>

    // ==================== Background Work ====================

    /**
     * Schedules periodic cloud backup worker.
     *
     * Called on app startup if cloud sync is enabled.
     */
    fun scheduleCloudBackup()

    /**
     * Cancels scheduled cloud backup worker.
     *
     * Called when user disables cloud sync or signs out.
     */
    fun cancelCloudBackup()
}
