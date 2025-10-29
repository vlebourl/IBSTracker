package com.tiarkaerell.ibstracker.data.model.backup

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Lightweight metadata for displaying backup information in UI without loading full file.
 *
 * @property fileName Same as BackupFile.fileName
 * @property timestamp Unix epoch
 * @property humanReadableDate e.g., "Oct 27, 2025 2:30 PM"
 * @property relativeTime e.g., "2 minutes ago", "1 hour ago"
 * @property sizeMB e.g., "2.1 MB"
 * @property location LOCAL or CLOUD
 * @property databaseVersion For compatibility checking
 * @property isLatest True if most recent backup
 */
data class BackupMetadata(
    val fileName: String,
    val timestamp: Long,
    val humanReadableDate: String,
    val relativeTime: String,
    val sizeMB: String,
    val location: BackupLocation,
    val databaseVersion: Int,
    val isLatest: Boolean = false
)

/**
 * Extension function to convert BackupFile to BackupMetadata.
 */
fun BackupFile.toMetadata(): BackupMetadata {
    val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    val humanDate = sdf.format(Date(timestamp))

    val relativeTime = when (val diff = System.currentTimeMillis() - timestamp) {
        in 0..60_000 -> "Just now"
        in 60_000..3_600_000 -> "${diff / 60_000} minutes ago"
        in 3_600_000..86_400_000 -> "${diff / 3_600_000} hours ago"
        else -> "${diff / 86_400_000} days ago"
    }

    val sizeMB = "%.1f MB".format(sizeBytes / 1_048_576.0)

    return BackupMetadata(
        fileName = fileName,
        timestamp = timestamp,
        humanReadableDate = humanDate,
        relativeTime = relativeTime,
        sizeMB = sizeMB,
        location = location,
        databaseVersion = databaseVersion,
        isLatest = false
    )
}
