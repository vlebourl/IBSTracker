package com.tiarkaerell.ibstracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RestorePage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.backup.BackupFile
import com.tiarkaerell.ibstracker.data.model.backup.BackupLocation
import com.tiarkaerell.ibstracker.data.model.backup.toMetadata

/**
 * List item for displaying a single backup file.
 *
 * Material Design 3 Component:
 * - Uses ListItem for consistent layout
 * - Shows backup metadata (timestamp, size, version)
 * - Provides restore and delete actions
 * - Indicates compatibility status
 *
 * @param backupFile The backup file to display
 * @param isCompatible Whether backup is compatible with current DB version
 * @param onRestoreClick Callback when restore button clicked
 * @param onDeleteClick Callback when delete button clicked
 * @param modifier Optional modifier
 */
@Composable
fun BackupListItem(
    backupFile: BackupFile,
    isCompatible: Boolean,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val metadata = backupFile.toMetadata()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = metadata.humanReadableDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                Column {
                    Text(
                        text = "${metadata.relativeTime} • ${metadata.sizeMB}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!isCompatible) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Incompatible",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Version ${backupFile.databaseVersion} (incompatible)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "Version ${backupFile.databaseVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            leadingContent = {
                Icon(
                    imageVector = if (metadata.isLatest) Icons.Default.CheckCircle else Icons.Default.RestorePage,
                    contentDescription = if (metadata.isLatest) "Latest backup" else "Backup",
                    tint = if (metadata.isLatest)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
            },
            trailingContent = {
                Row {
                    // Restore button
                    IconButton(
                        onClick = onRestoreClick,
                        enabled = isCompatible
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestorePage,
                            contentDescription = "Restore",
                            tint = if (isCompatible)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        )
                    }

                    // Delete button
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}
