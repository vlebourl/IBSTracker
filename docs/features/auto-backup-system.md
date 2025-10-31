# Feature: Automatic Backup System

## Overview

Implement a comprehensive two-tier backup system to protect user data from loss due to app crashes, accidental deletions, device failures, or device loss.

## Problem Statement

Users are actively tracking valuable health data (food intake, symptoms, correlations) in the IBS Tracker app. Currently, there is no automatic backup mechanism, which means:
- Accidental deletions are permanent and unrecoverable
- App crashes or corruption can result in complete data loss
- Device loss or failure means losing months of tracked data
- Users have no disaster recovery option
- No way to transfer data to a new device

This creates significant risk for users who depend on this app for managing their IBS condition and communicating patterns to healthcare providers.

## Solution

A two-tier automatic backup system:

### Tier 1: Local Backup (Immediate Protection)
- **Trigger**: Automatically after every data change (food/symptom add/edit/delete)
- **Location**: App-specific storage on device
- **Retention**: Keep last 7 backups
- **Purpose**: Immediate recovery from accidental deletions or app corruption
- **No network required**: Works offline, completely private

### Tier 2: Google Drive Backup (Disaster Recovery)
- **Trigger**: Scheduled daily at 2:00 AM
- **Location**: Google Drive app folder (private to the app)
- **Retention**: Keep last 30 days of backups
- **Purpose**: Long-term disaster recovery (device loss, device reset)
- **Cloud storage**: Accessible from any device after Google Sign-In

## User Stories

### US1: Local Backup After Changes (Priority: P0 - Critical)
**As a** user tracking my IBS symptoms
**I want** my data automatically backed up after every change
**So that** I can recover from accidental deletions or app crashes immediately

**Acceptance Criteria:**
- ✅ Backup created automatically after adding food item
- ✅ Backup created automatically after deleting food item
- ✅ Backup created automatically after editing food item
- ✅ Backup created automatically after adding symptom
- ✅ Backup created automatically after deleting symptom
- ✅ Backup created automatically after editing symptom
- ✅ Up to 7 most recent backups are kept
- ✅ Older backups are automatically deleted (rolling deletion)
- ✅ Backups stored in app-specific directory (survives app updates)
- ✅ Backups are deleted when app is uninstalled (no orphaned files)
- ✅ Backup operation is fast (< 200ms) and non-blocking
- ✅ No UI feedback for automatic backups (silent operation)

**Test Scenarios:**
1. Add food "Soja" → verify backup created with current timestamp
2. Delete symptom entry → verify new backup created
3. Create 10 backups → verify only last 7 remain
4. Check backup file size → verify reasonable (< 5MB for typical data)
5. Simulate app crash during backup → verify database not corrupted

### US2: Restore from Local Backup (Priority: P0 - Critical)
**As a** user who accidentally deleted important data
**I want** to restore from a recent local backup
**So that** I can recover my data without losing more than a few minutes of work

**Acceptance Criteria:**
- ✅ Settings screen shows "Backup & Restore" section
- ✅ Shows last backup timestamp (e.g., "2 minutes ago", "1 hour ago")
- ✅ Shows count of available local backups (e.g., "7 backups available")
- ✅ "Restore from local backup" button opens backup selection dialog
- ✅ Backup selection dialog shows list of backups with timestamps
- ✅ Each backup shows creation date/time (human-readable format)
- ✅ Each backup shows size (MB)
- ✅ Selecting backup shows confirmation dialog with warning
- ✅ Warning indicates current data will be replaced
- ✅ After restore, app shows success message with restored data count
- ✅ After restore, current database is replaced with backup
- ✅ After restore, app state refreshes to show restored data

**Test Scenarios:**
1. Create backup → delete all data → restore → verify data returns
2. Create 3 backups at different times → verify list shows all 3 with correct timestamps
3. Cancel restore operation → verify no data changed
4. Restore backup while viewing food list → verify UI refreshes automatically

### US3: Google Drive Daily Sync (Priority: P1 - High)
**As a** user tracking long-term IBS patterns
**I want** my data automatically backed up to Google Drive daily
**So that** I can recover my data even if I lose my device

**Acceptance Criteria:**
- ✅ WorkManager scheduled task runs daily at 2:00 AM
- ✅ Sync only runs when WiFi is available (constraint)
- ✅ Sync only runs when battery is not low (constraint)
- ✅ Sync only runs when device is idle (constraint)
- ✅ Creates database backup and uploads to Google Drive app folder
- ✅ Google Drive folder is private to the app (not visible in user's Drive)
- ✅ Keeps last 30 days of backups on Google Drive
- ✅ Deletes backups older than 30 days automatically
- ✅ Settings screen shows "Last sync" timestamp
- ✅ Settings screen shows "Next sync" scheduled time
- ✅ Settings screen shows sync status (Synced/Syncing/Failed/Never)
- ✅ "Backup now" button allows manual immediate sync
- ✅ Manual sync shows progress indicator
- ✅ Sync failures are logged but don't crash app
- ✅ Failed sync shows error message with retry option

**Test Scenarios:**
1. Wait for 2:00 AM → verify sync occurs automatically
2. Enable airplane mode → verify sync skips and reschedules
3. Trigger manual sync → verify immediate upload to Google Drive
4. Create 35 daily backups → verify only last 30 remain on Drive
5. Simulate network error during upload → verify graceful error handling

### US4: Restore from Google Drive (Priority: P1 - High)
**As a** user who lost or reset my device
**I want** to restore my data from Google Drive
**So that** I can continue tracking my IBS patterns on a new device

**Acceptance Criteria:**
- ✅ Settings screen shows "Restore from cloud" button
- ✅ Requires Google Sign-In if not already signed in
- ✅ Shows list of available cloud backups (up to 30)
- ✅ Each backup shows date, time, and size
- ✅ Most recent backup is highlighted/marked
- ✅ Selecting backup downloads from Google Drive
- ✅ Shows download progress indicator
- ✅ After download, shows confirmation dialog
- ✅ Restore replaces current database with cloud backup
- ✅ Success message shows restored data count
- ✅ App state refreshes to show restored data

**Test Scenarios:**
1. New device → sign in → restore from cloud → verify all data restored
2. Select old backup (7 days ago) → verify that version restored
3. Cancel during download → verify no data changed
4. Network interruption during download → verify graceful error with retry

### US5: Backup Settings & Configuration (Priority: P2 - Medium)
**As a** user who wants control over backup behavior
**I want** to configure backup settings
**So that** I can customize backup behavior to my preferences

**Acceptance Criteria:**
- ✅ Settings toggle: "Enable local backups" (default: ON)
- ✅ Settings toggle: "Enable Google Drive sync" (default: ON)
- ✅ When local backups disabled, no automatic backups created
- ✅ When cloud sync disabled, scheduled task doesn't run
- ✅ Manual "Backup now" always works (regardless of toggle state)
- ✅ Settings show backup storage usage (MB)
- ✅ "Clear all local backups" button with confirmation
- ✅ "Sign out of Google Drive" button (stops cloud sync)
- ✅ Settings persist across app restarts

**Test Scenarios:**
1. Disable local backups → add food → verify no backup created
2. Disable cloud sync → verify 2am task doesn't run
3. Clear all backups → verify local backups deleted
4. Sign out → verify cloud sync stops

## Technical Architecture

### Data Flow

```
User Action (Add/Edit/Delete Food/Symptom)
    ↓
DataRepository mutation method
    ↓
BackupManager.createLocalBackup()
    ↓
1. Copy database file to backup directory
2. Name: ibs-tracker-backup-{timestamp}.db
3. Delete old backups (keep only 7)
    ↓
Background operation (no UI blocking)

---

Daily at 2:00 AM (WorkManager)
    ↓
GoogleDriveBackupWorker.doWork()
    ↓
1. Create local backup
2. Authenticate with Google Drive
3. Upload to app folder
4. Delete backups older than 30 days
    ↓
Update sync status in Settings
```

### Components

#### BackupManager
**Responsibility**: Core backup/restore logic for both local and cloud

**Methods:**
```kotlin
suspend fun createLocalBackup(): BackupResult
suspend fun restoreLocalBackup(timestamp: Long): RestoreResult
fun getLocalBackups(): List<BackupMetadata>
fun deleteOldLocalBackups(keepCount: Int = 7)

suspend fun uploadToGoogleDrive(): UploadResult
suspend fun downloadFromGoogleDrive(backupId: String): DownloadResult
fun getCloudBackups(): List<BackupMetadata>
fun deleteOldCloudBackups(keepDays: Int = 30)
```

#### GoogleDriveBackupWorker (WorkManager)
**Responsibility**: Scheduled daily backup to Google Drive

**Schedule**:
- Periodic work (24 hours interval)
- Preferred time: 2:00 AM
- Constraints: WiFi, battery not low, device idle

#### BackupSettingsScreen (Composable)
**Responsibility**: UI for backup configuration and restore operations

**Sections:**
- Local Backups status and restore
- Google Drive sync status and restore
- Settings toggles
- Storage usage
- Manual operations

### Data Models

```kotlin
data class BackupMetadata(
    val timestamp: Long,
    val fileName: String,
    val sizeBytes: Long,
    val location: BackupLocation,
    val status: BackupStatus
)

enum class BackupLocation {
    LOCAL,
    GOOGLE_DRIVE
}

enum class BackupStatus {
    AVAILABLE,
    UPLOADING,
    DOWNLOADING,
    FAILED
}

sealed class BackupResult {
    data class Success(val metadata: BackupMetadata) : BackupResult()
    data class Error(val message: String, val cause: Throwable?) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val restoredEntries: Int) : RestoreResult()
    data class Error(val message: String, val cause: Throwable?) : RestoreResult()
}
```

### Storage Locations

#### Local Backups
```kotlin
// Persists across app updates, deleted on uninstall
val backupDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    ?.resolve("backups")
    ?: error("Cannot access backup directory")

// Naming convention
val fileName = "ibs-tracker-backup-${System.currentTimeMillis()}.db"
```

#### Google Drive
```kotlin
// App folder (private, not visible in user's Drive)
val driveFolder = "appDataFolder"

// Naming convention (same as local)
val fileName = "ibs-tracker-backup-${timestamp}.db"
```

## Technical Constraints

### Performance
- Local backup creation: < 200ms (non-blocking)
- Local backup restore: < 2 seconds
- Cloud upload: < 10 seconds (for typical 2MB database)
- Cloud download: < 10 seconds

### Storage
- Local backups: Max 7 files × ~2MB = ~14MB
- Cloud backups: Max 30 files × ~2MB = ~60MB
- Minimal impact on user's Google Drive storage quota

### Network
- Cloud sync requires internet connection
- WiFi-only by default (no mobile data usage)
- Graceful handling of network failures

### Permissions
- Local backups: No special permissions (app-specific storage)
- Google Drive: Requires Google Sign-In (OAuth)
- No WRITE_EXTERNAL_STORAGE permission needed

## Dependencies

### Existing
- Room Database: `androidx.room:room-runtime:2.6.1`
- Kotlin Coroutines: Already in use
- Jetpack Compose: Already in use for UI

### New
- WorkManager: `androidx.work:work-runtime-ktx:2.9.0`
- Google Drive API: `com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0`
- Google Sign-In: `com.google.android.gms:play-services-auth:20.7.0`

## User Interface

### Settings Screen - Backup & Restore Section

```
┌─────────────────────────────────────────┐
│ Settings                                │
├─────────────────────────────────────────┤
│                                         │
│ 💾 Backup & Restore                     │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Local Backups                       │ │
│ │                                     │ │
│ │ Last backup: 2 minutes ago          │ │
│ │ Available backups: 7                │ │
│ │                                     │ │
│ │ [Restore from local backup...]      │ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ Google Drive Sync                   │ │
│ │                                     │ │
│ │ Status: ✓ Synced                    │ │
│ │ Last sync: Today at 2:00 AM         │ │
│ │ Next sync: Tomorrow at 2:00 AM      │ │
│ │                                     │ │
│ │ [Backup now]  [Restore from cloud...]│ │
│ └─────────────────────────────────────┘ │
│                                         │
│ ⚙️ Backup Settings                      │
│                                         │
│ ☑ Enable local backups                 │
│ ☑ Enable Google Drive sync              │
│                                         │
│ Storage used: 14 MB (local)             │
│               52 MB (cloud)             │
│                                         │
│ [Clear all local backups]               │
│                                         │
└─────────────────────────────────────────┘
```

### Restore Dialog

```
┌─────────────────────────────────────────┐
│ Restore from Local Backup               │
├─────────────────────────────────────────┤
│                                         │
│ Select a backup to restore:             │
│                                         │
│ ○ Today at 3:42 PM         (2.1 MB)    │
│ ○ Today at 2:15 PM         (2.1 MB)    │
│ ○ Today at 10:30 AM        (2.0 MB)    │
│ ○ Yesterday at 8:45 PM     (2.0 MB)    │
│ ○ Yesterday at 3:20 PM     (1.9 MB)    │
│ ○ 2 days ago at 9:10 PM    (1.9 MB)    │
│ ○ 3 days ago at 7:30 PM    (1.8 MB)    │
│                                         │
│           [Cancel]  [Restore]           │
└─────────────────────────────────────────┘
```

### Restore Confirmation

```
┌─────────────────────────────────────────┐
│ ⚠️  Confirm Restore                      │
├─────────────────────────────────────────┤
│                                         │
│ This will replace your current data     │
│ with the backup from:                   │
│                                         │
│ Today at 2:15 PM                        │
│                                         │
│ Your current data will be backed up     │
│ before restoring.                       │
│                                         │
│ Continue?                               │
│                                         │
│           [Cancel]  [Restore]           │
└─────────────────────────────────────────┘
```

## Edge Cases & Error Handling

### Local Backup Failures
1. **Insufficient storage**: Show warning, skip backup, alert user
2. **Backup directory not accessible**: Log error, show notification
3. **Database locked during backup**: Retry once, then skip
4. **Corrupted backup file**: Mark as invalid, exclude from restore list

### Google Drive Failures
1. **No internet connection**: Skip sync, reschedule for next day
2. **Google Sign-In expired**: Prompt user to sign in again
3. **Upload timeout**: Retry with exponential backoff (3 attempts)
4. **Drive quota exceeded**: Show error, suggest clearing old backups
5. **Download corrupted**: Show error, allow retry or select different backup

### Restore Failures
1. **Backup file corrupted**: Show error, prevent restore
2. **Database version mismatch**: Show error, explain incompatibility
3. **Restore interrupted**: Rollback to pre-restore state
4. **App crash during restore**: Auto-recover on next app start

## Success Metrics

### Data Protection
- 0 user reports of permanent data loss
- < 1% failed local backups
- < 5% failed cloud syncs (due to network issues)

### Performance
- Local backup creation: < 200ms average
- No user-reported app slowdowns due to backups
- Cloud sync battery impact: < 1% per day

### Adoption
- 80%+ users have local backups enabled (default ON)
- 60%+ users have cloud sync enabled
- 10%+ users have used restore feature

## Migration & Rollout

### Phase 1: Local Backups (v1.13.0)
- Implement local backup after changes
- Implement local restore UI
- Release to production
- Monitor for 2 weeks

### Phase 2: Google Drive Sync (v1.13.0 or v1.14.0)
- Add Google Drive integration
- Add cloud restore UI
- Beta test with opt-in users
- Full rollout after validation

### Rollback Plan
- Settings toggle to disable feature
- No breaking changes to database schema
- Can safely remove feature in update

## Testing Strategy

### Unit Tests
- `BackupManager.createLocalBackup()` creates file with correct name
- `BackupManager.deleteOldLocalBackups()` keeps only N backups
- `BackupManager.getLocalBackups()` returns sorted list
- Backup file naming and timestamp parsing

### Integration Tests
- Create backup → restore → verify data integrity
- Add 10 foods → create 10 backups → verify only 7 remain
- Simulate database with 1000 entries → verify backup/restore performance

### Instrumented Tests
- Full backup/restore cycle on real device
- Google Drive upload/download integration
- WorkManager scheduled task execution

### Manual Testing
1. Add food → verify backup created (check file timestamp)
2. Delete symptom → verify new backup created
3. Restore from 2-day-old backup → verify UI refreshes
4. Trigger manual cloud sync → verify upload to Google Drive
5. Uninstall app → reinstall → restore from cloud → verify data returns

## Open Questions

1. **Should we backup during active data entry?**
   - Option A: Debounce backups (wait 5 seconds after last change)
   - Option B: Backup immediately on every change
   - **Decision needed**: Option B (immediate) for maximum data protection

2. **Should restore require confirmation for recent backups?**
   - Option A: Always require confirmation
   - Option B: Skip confirmation for backups < 1 hour old
   - **Decision needed**: Option A (always confirm) for safety

3. **Should we show backup size in UI?**
   - Yes, helps users understand storage impact
   - Show in MB with 1 decimal place

4. **Should manual "Backup now" require WiFi?**
   - No, allow on mobile data (user initiated)
   - Show warning if on mobile data

## Future Enhancements (Out of Scope)

- Export backup as .zip file for manual sharing
- Import backup from file picker
- Backup encryption (password protected)
- Automatic restore on app crash detection
- Multi-device sync (real-time, not just daily)
- Backup to other cloud providers (Dropbox, OneDrive)
- Selective restore (only foods, only symptoms)
- Backup history analytics (show data growth over time)

## References

- [WorkManager Documentation](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Google Drive API Documentation](https://developers.google.com/drive/api/guides/about-sdk)
- [Room Database Backup Best Practices](https://developer.android.com/training/data-storage/room#database-backups)
- [Android Backup Service](https://developer.android.com/guide/topics/data/backup)

---

**Document Version**: 1.0
**Created**: 2025-10-27
**Status**: Ready for specification
**Target Release**: v1.13.0
