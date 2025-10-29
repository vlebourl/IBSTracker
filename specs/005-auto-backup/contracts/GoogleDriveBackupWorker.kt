/**
 * API Contract: GoogleDriveBackupWorker
 *
 * WorkManager Worker for scheduled Google Drive cloud synchronization.
 * Runs daily at 2:00 AM (with 1-hour flex period: 1:00-2:00 AM).
 *
 * @see research.md Section 3 (WorkManager Scheduling) for scheduling patterns
 * @see research.md Section 4 (Google Drive API) for upload patterns
 */
abstract class GoogleDriveBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    /**
     * Executes the cloud backup workflow.
     *
     * Workflow steps:
     * 1. Check if cloud sync is enabled (BackupSettings.cloudSyncEnabled)
     * 2. Check if Google Sign-In is active (BackupSettings.isGoogleSignedIn)
     * 3. Create local backup (if not already created today)
     * 4. Upload backup to Google Drive appDataFolder
     * 5. Update SyncStatus (SYNCING → SYNCED/FAILED)
     * 6. Update BackupSettings.lastCloudSyncTimestamp
     * 7. Cleanup old cloud backups (keep 30 most recent)
     *
     * Constraints (automatically enforced by WorkManager):
     * - WiFi only (NetworkType.UNMETERED)
     * - Device charging (setRequiresCharging = true)
     * - Battery not low (setRequiresBatteryNotLow = true)
     *
     * Retry policy:
     * - Return Result.retry() for transient errors (network, timeout)
     * - Return Result.failure() for permanent errors (auth failed, quota exceeded)
     * - Maximum 3 retry attempts with exponential backoff
     *
     * @return Result.success() if sync completed, Result.retry() for transient errors,
     *         Result.failure() for permanent errors
     */
    abstract override suspend fun doWork(): Result

    /**
     * Uploads a backup file to Google Drive appDataFolder.
     *
     * Upload method: Multipart upload (recommended for files <5MB)
     *
     * File metadata:
     * - name: Same as local filename (ibstracker_v{version}_{timestamp}.db)
     * - parents: ["appDataFolder"]
     * - mimeType: "application/x-sqlite3"
     *
     * Performance requirement: Must complete in <10 seconds for 2MB database
     *
     * @param backupFile Local backup file to upload
     * @param driveService Authenticated Google Drive service
     * @param onProgress Callback for upload progress (0-100)
     * @return Drive File ID if upload successful, null if failed
     * @throws IOException if network error occurs
     * @throws SecurityException if authentication expired
     */
    protected abstract suspend fun uploadToGoogleDrive(
        backupFile: BackupFile,
        driveService: Drive,
        onProgress: (Int) -> Unit = {}
    ): String?

    /**
     * Lists all backup files in Google Drive appDataFolder.
     *
     * Query: 'appDataFolder' in parents and name contains 'ibstracker_v'
     * Order: createdTime desc (newest first)
     *
     * @param driveService Authenticated Google Drive service
     * @return List of BackupFile representing cloud backups
     * @throws IOException if network error occurs
     */
    protected abstract suspend fun listCloudBackups(driveService: Drive): List<BackupFile>

    /**
     * Deletes old cloud backups, keeping only the 30 most recent.
     *
     * Cleanup policy:
     * - Sort backups by createdTime descending
     * - Keep first 30 backups
     * - Delete all backups after position 30
     *
     * @param driveService Authenticated Google Drive service
     * @return Number of backups deleted
     * @throws IOException if network error occurs
     */
    protected abstract suspend fun cleanupOldCloudBackups(driveService: Drive): Int

    /**
     * Calculates total storage usage of cloud backups.
     *
     * Queries all backup files and sums their sizes.
     *
     * @param driveService Authenticated Google Drive service
     * @return Total size in bytes of all cloud backups
     */
    protected abstract suspend fun calculateCloudStorageUsage(driveService: Drive): Long

    /**
     * Checks if the user is signed in to Google and has granted Drive access.
     *
     * Required scope: DriveScopes.DRIVE_APPDATA
     *
     * @return true if authenticated and scope granted, false otherwise
     */
    protected abstract suspend fun isGoogleAuthenticated(): Boolean

    companion object {
        /**
         * Schedules the periodic cloud backup worker.
         *
         * Schedule: Every 24 hours with 1-hour flex period (executes 1:00-2:00 AM)
         * Unique work name: "google_drive_backup"
         * Existing work policy: KEEP (don't replace if already scheduled)
         *
         * Constraints:
         * - NetworkType.UNMETERED (WiFi only)
         * - RequiresCharging = true
         * - RequiresBatteryNotLow = true
         *
         * Backoff: Exponential with minimum delay (default 30 seconds)
         *
         * @param context Application context
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .build()

            val backupRequest = PeriodicWorkRequestBuilder<GoogleDriveBackupWorker>(
                24, TimeUnit.HOURS,  // Repeat interval
                1, TimeUnit.HOURS    // Flex interval (execution window)
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "google_drive_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                backupRequest
            )
        }

        /**
         * Triggers a manual cloud backup immediately.
         *
         * Bypasses schedule and constraints. Useful for "Backup now" button.
         *
         * @param context Application context
         * @return WorkRequest ID for tracking progress
         */
        fun triggerManualBackup(context: Context): UUID {
            val request = OneTimeWorkRequestBuilder<GoogleDriveBackupWorker>()
                // No constraints for manual backup - always run
                .build()

            WorkManager.getInstance(context).enqueue(request)
            return request.id
        }

        /**
         * Cancels all scheduled cloud backups.
         *
         * Called when user disables cloud sync or signs out of Google.
         *
         * @param context Application context
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork("google_drive_backup")
        }
    }
}
