package com.tiarkaerell.ibstracker.data.model.backup

/**
 * Represents the storage location of a backup file.
 */
enum class BackupLocation {
    /**
     * Backup stored in app-specific local storage (filesDir).
     * Persists across app updates but deleted on uninstall.
     */
    LOCAL,

    /**
     * Backup stored in Google Drive appDataFolder.
     * Private to the app, not visible in user's Drive UI.
     * Automatically deleted when app is uninstalled.
     */
    CLOUD
}
