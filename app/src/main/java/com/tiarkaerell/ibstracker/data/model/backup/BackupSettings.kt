package com.tiarkaerell.ibstracker.data.model.backup

/**
 * User preferences for backup behavior, stored in DataStore.
 *
 * @property localBackupsEnabled Toggle for automatic local backups
 * @property cloudSyncEnabled Toggle for Google Drive sync
 * @property lastLocalBackupTimestamp Unix epoch of last local backup
 * @property lastCloudSyncTimestamp Unix epoch of last cloud sync
 * @property googleAccountEmail Signed-in Google account
 * @property isGoogleSignedIn Google Sign-In status
 * @property localStorageUsageBytes Total size of local backups
 * @property cloudStorageUsageBytes Total size of cloud backups (estimated)
 * @property totalBackupsCount Total number of backups (local + cloud)
 * @property localBackupsCount Number of local backups
 * @property cloudBackupsCount Number of cloud backups
 */
data class BackupSettings(
    val localBackupsEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = true,
    val lastLocalBackupTimestamp: Long? = null,
    val lastCloudSyncTimestamp: Long? = null,
    val googleAccountEmail: String? = null,
    val isGoogleSignedIn: Boolean = false,
    val localStorageUsageBytes: Long = 0,
    val cloudStorageUsageBytes: Long = 0,
    val totalBackupsCount: Int = 0,
    val localBackupsCount: Int = 0,
    val cloudBackupsCount: Int = 0
) {
    /**
     * Validates consistency of settings.
     */
    fun isValid(): Boolean {
        return localStorageUsageBytes >= 0
                && cloudStorageUsageBytes >= 0
                && totalBackupsCount >= 0
                && localBackupsCount >= 0
                && cloudBackupsCount >= 0
                && (!isGoogleSignedIn || googleAccountEmail != null)
    }
}
