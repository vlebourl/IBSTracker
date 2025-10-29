package com.tiarkaerell.ibstracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.backup.BackupSettings
import java.text.SimpleDateFormat
import java.util.*

/**
 * Status card showing backup overview information.
 *
 * Material Design 3 Component:
 * - Uses Card with outlined variant
 * - Shows last backup timestamp
 * - Shows storage usage
 * - Shows backup count
 * - Indicates cloud sync status
 *
 * @param settings Current backup settings
 * @param modifier Optional modifier
 */
@Composable
fun BackupStatusCard(
    settings: BackupSettings,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Backup Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Icon(
                    imageVector = if (settings.cloudSyncEnabled && settings.isGoogleSignedIn)
                        Icons.Default.CloudDone
                    else
                        Icons.Default.CloudOff,
                    contentDescription = if (settings.cloudSyncEnabled && settings.isGoogleSignedIn)
                        "Cloud sync enabled"
                    else
                        "Cloud sync disabled",
                    tint = if (settings.cloudSyncEnabled && settings.isGoogleSignedIn)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Last local backup
            StatusRow(
                label = "Last local backup",
                value = formatTimestamp(settings.lastLocalBackupTimestamp),
                icon = Icons.Default.Storage
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Last cloud sync
            if (settings.cloudSyncEnabled) {
                StatusRow(
                    label = "Last cloud sync",
                    value = formatTimestamp(settings.lastCloudSyncTimestamp),
                    icon = Icons.Default.CloudDone
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Storage usage
            StatusRow(
                label = "Local storage used",
                value = formatBytes(settings.localStorageUsageBytes),
                icon = Icons.Default.Storage
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Backup count
            StatusRow(
                label = "Total backups",
                value = "${settings.localBackupsCount} local" +
                        if (settings.cloudSyncEnabled) " • ${settings.cloudBackupsCount} cloud" else "",
                icon = Icons.Default.Storage
            )
        }
    }
}

@Composable
private fun StatusRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "Never"

    val sdf = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
