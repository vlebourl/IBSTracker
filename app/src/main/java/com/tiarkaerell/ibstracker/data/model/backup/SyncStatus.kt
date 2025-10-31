package com.tiarkaerell.ibstracker.data.model.backup

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents the current state of cloud synchronization.
 *
 * @property status Current sync state
 * @property lastSyncTimestamp Unix epoch of last successful sync (null if never)
 * @property nextSyncTimestamp Unix epoch of next scheduled sync
 * @property errorMessage Error description if status is FAILED
 * @property uploadProgress Upload progress percentage (0-100)
 * @property downloadProgress Download progress percentage (0-100)
 */
data class SyncStatus(
    val status: SyncState,
    val lastSyncTimestamp: Long? = null,
    val nextSyncTimestamp: Long? = null,
    val errorMessage: String? = null,
    val uploadProgress: Int = 0,
    val downloadProgress: Int = 0
) {
    /**
     * Converts sync status to human-readable display string.
     */
    fun toDisplayString(): String = when (status) {
        SyncState.SYNCED -> {
            val time = lastSyncTimestamp?.let {
                SimpleDateFormat("MMM dd h:mm a", Locale.getDefault()).format(Date(it))
            } ?: "Unknown"
            "Last sync: $time"
        }
        SyncState.SYNCING -> "Syncing... ${uploadProgress}%"
        SyncState.FAILED -> "Sync failed: $errorMessage"
        SyncState.NEVER -> "Never synced"
    }
}
