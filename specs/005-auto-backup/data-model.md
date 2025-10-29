# Data Model: Automatic Backup System

**Feature**: 005-auto-backup
**Date**: 2025-10-27
**Source**: Extracted from [spec.md](spec.md) and [research.md](research.md)

## Overview

This document defines the data models, entities, and relationships for the automatic backup system. The system uses a combination of file-based backups, DataStore preferences, and optional Room entities for metadata persistence.

---

## Core Entities

### 1. BackupFile (Domain Model)

Represents a physical backup file on disk or in cloud storage.

```kotlin
data class BackupFile(
    val id: String,                    // UUID for unique identification
    val fileName: String,              // e.g., "ibstracker_v10_20251027_140530.db"
    val filePath: String,              // Absolute path to file (local) or Drive file ID (cloud)
    val location: BackupLocation,      // LOCAL or CLOUD
    val timestamp: Long,               // Unix epoch (milliseconds) when backup was created
    val sizeBytes: Long,               // File size in bytes
    val databaseVersion: Int,          // Room database version at backup time
    val checksum: String,              // SHA-256 checksum (64 hex characters)
    val status: BackupStatus,          // Current status of this backup
    val createdAt: Long = System.currentTimeMillis()
)

enum class BackupLocation {
    LOCAL,   // Stored in app-specific filesDir
    CLOUD    // Stored in Google Drive appDataFolder
}

enum class BackupStatus {
    AVAILABLE,      // Ready to restore
    UPLOADING,      // Currently uploading to cloud
    DOWNLOADING,    // Currently downloading from cloud
    FAILED,         // Upload/download failed
    CORRUPTED       // Checksum verification failed
}
```

**Validation Rules**:
- `fileName` must match pattern: `ibstracker_v{version}_{yyyyMMdd}_{HHmmss}.db`
- `checksum` must be exactly 64 hexadecimal characters (SHA-256)
- `databaseVersion` must match `AppDatabase.DATABASE_VERSION` for restore compatibility
- `sizeBytes` must be > 0 and < 10MB (sanity check)

**State Transitions**:
```
LOCAL backups:  AVAILABLE
CLOUD backups:  AVAILABLE → UPLOADING → AVAILABLE (success)
                         → UPLOADING → FAILED (error)
Restore:        CLOUD → DOWNLOADING → AVAILABLE (success)
                     → DOWNLOADING → FAILED (error)
Corrupted:      Any → CORRUPTED (checksum mismatch)
```

---

### 2. BackupMetadata (Value Object)

Lightweight metadata for displaying backup information in UI without loading full file.

```kotlin
data class BackupMetadata(
    val fileName: String,              // Same as BackupFile.fileName
    val timestamp: Long,               // Unix epoch
    val humanReadableDate: String,     // e.g., "Oct 27, 2025 2:30 PM"
    val relativeTime: String,          // e.g., "2 minutes ago", "1 hour ago"
    val sizeMB: String,                // e.g., "2.1 MB"
    val location: BackupLocation,      // LOCAL or CLOUD
    val databaseVersion: Int,          // For compatibility checking
    val isLatest: Boolean = false      // True if most recent backup
)
```

**Computed Fields**:
```kotlin
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
        databaseVersion = databaseVersion
    )
}
```

---

### 3. SyncStatus (Domain Model)

Represents the current state of cloud synchronization.

```kotlin
data class SyncStatus(
    val status: SyncState,             // Current sync state
    val lastSyncTimestamp: Long?,      // Unix epoch of last successful sync (null if never)
    val nextSyncTimestamp: Long?,      // Unix epoch of next scheduled sync
    val errorMessage: String?,         // Error description if status is FAILED
    val uploadProgress: Int = 0,       // Upload progress percentage (0-100)
    val downloadProgress: Int = 0      // Download progress percentage (0-100)
)

enum class SyncState {
    SYNCED,     // Last sync completed successfully
    SYNCING,    // Currently syncing (uploading or downloading)
    FAILED,     // Last sync attempt failed
    NEVER       // No sync has ever been performed
}
```

**Display Logic**:
```kotlin
fun SyncStatus.toDisplayString(): String = when (status) {
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
```

---

### 4. BackupSettings (Preferences Model)

User preferences for backup behavior, stored in DataStore.

```kotlin
data class BackupSettings(
    val localBackupsEnabled: Boolean = true,      // Toggle for automatic local backups
    val cloudSyncEnabled: Boolean = true,         // Toggle for Google Drive sync
    val lastLocalBackupTimestamp: Long? = null,   // Unix epoch of last local backup
    val lastCloudSyncTimestamp: Long? = null,     // Unix epoch of last cloud sync
    val googleAccountEmail: String? = null,       // Signed-in Google account
    val isGoogleSignedIn: Boolean = false,        // Google Sign-In status
    val localStorageUsageBytes: Long = 0,         // Total size of local backups
    val cloudStorageUsageBytes: Long = 0,         // Total size of cloud backups (estimated)
    val totalBackupsCount: Int = 0,               // Total number of backups (local + cloud)
    val localBackupsCount: Int = 0,               // Number of local backups
    val cloudBackupsCount: Int = 0                // Number of cloud backups
)
```

**DataStore Implementation**:
```kotlin
// Define preferences keys
object BackupPreferencesKeys {
    val LOCAL_BACKUPS_ENABLED = booleanPreferencesKey("local_backups_enabled")
    val CLOUD_SYNC_ENABLED = booleanPreferencesKey("cloud_sync_enabled")
    val LAST_LOCAL_BACKUP = longPreferencesKey("last_local_backup")
    val LAST_CLOUD_SYNC = longPreferencesKey("last_cloud_sync")
    val GOOGLE_ACCOUNT_EMAIL = stringPreferencesKey("google_account_email")
    val IS_GOOGLE_SIGNED_IN = booleanPreferencesKey("is_google_signed_in")
}

// Repository methods
class BackupPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    val settingsFlow: Flow<BackupSettings> = dataStore.data.map { prefs ->
        BackupSettings(
            localBackupsEnabled = prefs[LOCAL_BACKUPS_ENABLED] ?: true,
            cloudSyncEnabled = prefs[CLOUD_SYNC_ENABLED] ?: true,
            lastLocalBackupTimestamp = prefs[LAST_LOCAL_BACKUP],
            lastCloudSyncTimestamp = prefs[LAST_CLOUD_SYNC],
            googleAccountEmail = prefs[GOOGLE_ACCOUNT_EMAIL],
            isGoogleSignedIn = prefs[IS_GOOGLE_SIGNED_IN] ?: false
        )
    }

    suspend fun updateLocalBackupsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[LOCAL_BACKUPS_ENABLED] = enabled
        }
    }

    suspend fun recordLocalBackup(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_LOCAL_BACKUP] = timestamp
        }
    }
}
```

**Validation Rules**:
- Storage usage values must be >= 0
- Backup counts must be >= 0 and match actual file count
- If `isGoogleSignedIn` is false, `googleAccountEmail` must be null
- If `cloudSyncEnabled` is true, `isGoogleSignedIn` should be true (or prompt sign-in)

---

## Result Models

### 5. BackupResult (Operation Result)

Result of a backup operation (local or cloud).

```kotlin
sealed class BackupResult {
    data class Success(
        val backupFile: BackupFile,
        val durationMs: Long            // Time taken to create backup
    ) : BackupResult()

    data class Failure(
        val error: BackupError,
        val message: String,
        val cause: Throwable? = null
    ) : BackupResult()
}

enum class BackupError {
    STORAGE_FULL,           // Device storage is full
    DATABASE_LOCKED,        // Database is locked by another process
    CHECKPOINT_FAILED,      // WAL checkpoint failed
    COPY_FAILED,            // File copy operation failed
    CHECKSUM_MISMATCH,      // Integrity verification failed
    UPLOAD_FAILED,          // Google Drive upload failed
    AUTHENTICATION_FAILED,  // Google Sign-In failed or expired
    NETWORK_UNAVAILABLE,    // No network connection
    UNKNOWN                 // Unexpected error
}
```

---

### 6. RestoreResult (Operation Result)

Result of a restore operation.

```kotlin
sealed class RestoreResult {
    data class Success(
        val itemsRestored: Int,         // Number of food/symptom entries restored
        val backupFile: BackupFile,     // Source backup file
        val durationMs: Long            // Time taken to restore
    ) : RestoreResult()

    data class Failure(
        val error: RestoreError,
        val message: String,
        val cause: Throwable? = null
    ) : RestoreResult()
}

enum class RestoreError {
    FILE_NOT_FOUND,         // Backup file doesn't exist
    FILE_CORRUPTED,         // Checksum verification failed
    VERSION_MISMATCH,       // Database version incompatible
    DOWNLOAD_FAILED,        // Cloud download failed
    RESTORE_INTERRUPTED,    // App crash/force close during restore
    NETWORK_UNAVAILABLE,    // No network for cloud restore
    UNKNOWN                 // Unexpected error
}
```

---

## Relationships

```
BackupSettings (DataStore)
    ↓
    ├── Local Backups (filesDir)
    │   ├── BackupFile (ibstracker_v10_20251027_140530.db)
    │   ├── BackupFile (ibstracker_v10_20251027_123015.db)
    │   └── ... (up to 7 files)
    │
    └── Cloud Backups (Google Drive appDataFolder)
        ├── BackupFile (ibstracker_v10_20251027_140530.db)
        ├── BackupFile (ibstracker_v10_20251026_020100.db)
        └── ... (up to 30 files)

Each BackupFile has:
    - Physical file (.db)
    - Checksum file (.sha256)
    - BackupMetadata (derived)

SyncStatus ←→ CloudSyncWorker (WorkManager)
    - Tracks current sync operation
    - Updated during upload/download
```

---

## Storage Paths

### Local Backups

```kotlin
// Base directory (app-specific, private)
val backupDir = context.filesDir

// Backup file path
val backupFilePath = File(backupDir, "ibstracker_v${version}_${timestamp}.db")

// Checksum file path
val checksumFilePath = File(backupDir, "ibstracker_v${version}_${timestamp}.db.sha256")

// Example:
// /data/data/com.tiarkaerell.ibstracker/files/ibstracker_v10_20251027_140530.db
// /data/data/com.tiarkaerell.ibstracker/files/ibstracker_v10_20251027_140530.db.sha256
```

**Cleanup Policy**: Keep only 7 most recent backups
```kotlin
fun cleanupOldLocalBackups(backupDir: File, retentionCount: Int = 7) {
    val backupFiles = backupDir.listFiles { file ->
        file.name.startsWith("ibstracker_v") && file.extension == "db"
    }?.sortedByDescending { it.lastModified() } ?: return

    backupFiles.drop(retentionCount).forEach { file ->
        file.delete()
        File("${file.path}.sha256").delete()  // Also delete checksum
    }
}
```

### Cloud Backups

```kotlin
// Google Drive appDataFolder (special folder)
// Not visible in user's Drive UI
// Accessible only by this app
// Automatically deleted on app uninstall

// Drive API file metadata
val fileMetadata = File().apply {
    name = "ibstracker_v${version}_${timestamp}.db"
    parents = listOf("appDataFolder")  // Special folder ID
}

// Query to list all cloud backups
val query = "'appDataFolder' in parents and name contains 'ibstracker_v'"
driveService.files().list()
    .setSpaces("appDataFolder")
    .setQ(query)
    .setOrderBy("createdTime desc")
    .execute()
```

**Cleanup Policy**: Keep only 30 most recent backups
```kotlin
suspend fun cleanupOldCloudBackups(driveService: Drive, retentionCount: Int = 30) {
    val query = "'appDataFolder' in parents and name contains 'ibstracker_v'"
    val files = driveService.files().list()
        .setSpaces("appDataFolder")
        .setQ(query)
        .setOrderBy("createdTime desc")
        .setFields("files(id, name, createdTime)")
        .execute()
        .files

    files.drop(retentionCount).forEach { file ->
        driveService.files().delete(file.id).execute()
    }
}
```

---

## Database Schema (Optional)

The backup system can work entirely with files and DataStore, but optionally you can persist backup metadata in Room for faster queries.

```kotlin
@Entity(tableName = "backup_metadata")
data class BackupMetadataEntity(
    @PrimaryKey val id: String,        // UUID
    val fileName: String,
    val filePath: String,
    val location: String,              // "LOCAL" or "CLOUD"
    val timestamp: Long,
    val sizeBytes: Long,
    val databaseVersion: Int,
    val checksum: String,
    val status: String,                // BackupStatus enum name
    val createdAt: Long
)

@Dao
interface BackupDao {
    @Query("SELECT * FROM backup_metadata WHERE location = :location ORDER BY timestamp DESC")
    fun getBackupsByLocation(location: String): Flow<List<BackupMetadataEntity>>

    @Query("SELECT * FROM backup_metadata WHERE location = 'LOCAL' ORDER BY timestamp DESC LIMIT 7")
    fun getRecentLocalBackups(): Flow<List<BackupMetadataEntity>>

    @Query("SELECT * FROM backup_metadata WHERE location = 'CLOUD' ORDER BY timestamp DESC LIMIT 30")
    fun getRecentCloudBackups(): Flow<List<BackupMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupMetadataEntity)

    @Delete
    suspend fun deleteBackup(backup: BackupMetadataEntity)

    @Query("DELETE FROM backup_metadata WHERE location = :location AND timestamp < :cutoffTimestamp")
    suspend fun deleteOldBackups(location: String, cutoffTimestamp: Long)
}
```

**Note**: This entity is optional. The system can scan filesystem/Drive for backups instead of persisting metadata.

---

## Computed Properties

### Storage Usage Calculation

```kotlin
fun calculateLocalStorageUsage(backupDir: File): Long {
    return backupDir.listFiles { file ->
        file.name.startsWith("ibstracker_v") && file.extension == "db"
    }?.sumOf { it.length() } ?: 0L
}

fun calculateCloudStorageUsage(driveService: Drive): Long {
    val query = "'appDataFolder' in parents and name contains 'ibstracker_v'"
    val files = driveService.files().list()
        .setSpaces("appDataFolder")
        .setQ(query)
        .setFields("files(size)")
        .execute()
        .files

    return files.sumOf { it.getSize() }
}
```

### Backup Filename Parsing

```kotlin
data class ParsedBackupFilename(
    val databaseVersion: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    val second: Int
) {
    val timestamp: Long
        get() = Calendar.getInstance().apply {
            set(year, month - 1, day, hour, minute, second)
        }.timeInMillis
}

fun parseBackupFilename(fileName: String): ParsedBackupFilename? {
    // Expected format: ibstracker_v10_20251027_140530.db
    val regex = Regex("""ibstracker_v(\d+)_(\d{4})(\d{2})(\d{2})_(\d{2})(\d{2})(\d{2})\.db""")
    val match = regex.matchEntire(fileName) ?: return null

    val (version, year, month, day, hour, minute, second) = match.destructured

    return ParsedBackupFilename(
        databaseVersion = version.toInt(),
        year = year.toInt(),
        month = month.toInt(),
        day = day.toInt(),
        hour = hour.toInt(),
        minute = minute.toInt(),
        second = second.toInt()
    )
}
```

---

## Data Flow

### Local Backup Flow

```
User Action (add/edit/delete food/symptom)
    ↓
DataRepository.insertFoodItem() / updateFoodItem() / deleteFoodItem()
    ↓
BackupManager.createLocalBackup()
    ↓
1. Check if backups enabled (BackupSettings.localBackupsEnabled)
2. Execute WAL checkpoint (PRAGMA wal_checkpoint(FULL))
3. Generate backup filename (ibstracker_v10_20251027_140530.db)
4. Copy database file with checksum calculation
5. Store checksum in .sha256 file
6. Update BackupSettings.lastLocalBackupTimestamp
7. Cleanup old backups (keep 7 most recent)
    ↓
BackupFile created (AVAILABLE status)
```

### Cloud Sync Flow

```
WorkManager scheduled task (2:00 AM daily)
    ↓
GoogleDriveBackupWorker.doWork()
    ↓
1. Check constraints (WiFi, charging, battery not low)
2. Check if sync enabled (BackupSettings.cloudSyncEnabled)
3. Check Google Sign-In status (BackupSettings.isGoogleSignedIn)
4. Create local backup (if not already created today)
5. Upload to Google Drive appDataFolder
6. Update SyncStatus (SYNCING → SYNCED/FAILED)
7. Update BackupSettings.lastCloudSyncTimestamp
8. Cleanup old cloud backups (keep 30 most recent)
    ↓
BackupFile created (AVAILABLE status, CLOUD location)
```

### Restore Flow

```
User taps "Restore from local/cloud backup"
    ↓
BackupViewModel.restoreBackup(backupFile)
    ↓
RestoreManager.restoreFromBackup(backupFile)
    ↓
1. Verify checksum (local) or download + verify (cloud)
2. Check database version compatibility
3. Create pre-restore safety backup
4. Close Room database
5. Copy backup file to database location
6. Reopen Room database (triggers migrations if needed)
7. Count restored items (query FoodItem + Symptom tables)
8. Notify UI to refresh all screens
    ↓
RestoreResult.Success(itemsRestored = 150)
```

---

## Summary

This data model supports:
- ✅ File-based backup storage (local + cloud)
- ✅ DataStore preferences for settings persistence
- ✅ Optional Room entities for metadata caching
- ✅ Strong typing with sealed result classes
- ✅ Computed properties for UI display
- ✅ Validation rules for data integrity
- ✅ State machines for backup/sync status
