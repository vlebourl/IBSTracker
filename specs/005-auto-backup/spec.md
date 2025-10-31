# Feature Specification: Automatic Backup System

**Feature Branch**: `005-auto-backup`
**Created**: 2025-10-27
**Status**: Draft
**Input**: User description: "add auto backup. read the file /Users/vlb/AndroidStudioProjects/IBSTracker/docs/features/auto-backup-system.md for details"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Local Backup After Changes (Priority: P0 - Critical)

Users tracking their IBS data need immediate protection against accidental deletions, app crashes, or data corruption. The system automatically creates a local backup after every data change (adding, editing, or deleting food items or symptoms).

**Why this priority**: This is the foundation of data protection. Without automatic local backups, users risk permanent data loss from simple mistakes or app issues. This delivers immediate value and protects the most common data loss scenarios.

**Independent Test**: Can be fully tested by adding/editing/deleting a food item or symptom, then verifying a backup file exists in local storage with current timestamp. Delivers immediate data recovery capability.

**Acceptance Scenarios**:

1. **Given** a user has the app open, **When** they add a new food item "Soja", **Then** a local backup is created automatically within 200ms with a timestamp-based filename
2. **Given** a user has 5 existing backups, **When** they delete a symptom entry, **Then** a new backup is created and the system maintains only the 7 most recent backups
3. **Given** a user has 10 existing backups, **When** the system performs backup cleanup, **Then** only the 7 most recent backups are kept and older backups are automatically deleted
4. **Given** the app experiences a crash during backup, **When** the app restarts, **Then** the database remains uncorrupted and functional
5. **Given** a user edits a food item, **When** the backup operation completes, **Then** the backup file size is under 5MB for typical data volume

---

### User Story 2 - Restore from Local Backup (Priority: P0 - Critical)

Users who accidentally delete important data or experience app issues need to quickly restore their data from a recent local backup without technical knowledge.

**Why this priority**: Backup is useless without restore capability. This completes the local protection story and must ship together with US1 to deliver actual value.

**Independent Test**: Create a backup with known data, delete all current data, use the restore feature to recover, and verify all data returns correctly. Delivers complete local recovery workflow.

**Acceptance Scenarios**:

1. **Given** a user opens Settings, **When** they navigate to Backup & Restore section, **Then** they see "Last backup: 2 minutes ago" and "7 backups available"
2. **Given** a user taps "Restore from local backup", **When** the backup list appears, **Then** they see 7 backups sorted by timestamp (newest first) with human-readable dates and file sizes
3. **Given** a user selects a backup from 2 hours ago, **When** they tap Restore, **Then** a confirmation dialog appears warning that current data will be replaced
4. **Given** a user confirms restore, **When** the operation completes, **Then** their database is replaced with the backup and they see "Restored 150 items" success message
5. **Given** a user is viewing the food list during restore, **When** the restore completes, **Then** the UI automatically refreshes to show the restored data

---

### User Story 3 - Google Drive Daily Sync (Priority: P1 - High)

Users tracking long-term IBS patterns need cloud backup to protect against device loss, device reset, or complete device failure. The system automatically syncs their data to Google Drive once daily at 2:00 AM.

**Why this priority**: Provides disaster recovery for device-level failures. Less critical than local backup because device loss is less frequent than accidental deletion. Can be deployed after local backup is validated.

**Independent Test**: Wait for scheduled 2:00 AM sync or trigger manual sync, verify file appears in Google Drive app folder with correct timestamp. Delivers cloud disaster recovery capability.

**Acceptance Scenarios**:

1. **Given** the time is 2:00 AM and the device is connected to WiFi, **When** the scheduled task runs, **Then** a backup is uploaded to Google Drive app folder
2. **Given** the device is on mobile data, **When** the 2:00 AM sync time arrives, **Then** the sync is skipped and rescheduled for the next day
3. **Given** a user opens Settings, **When** they view the Google Drive Sync section, **Then** they see "Status: ✓ Synced", "Last sync: Today at 2:00 AM", and "Next sync: Tomorrow at 2:00 AM"
4. **Given** a user taps "Backup now", **When** the manual sync runs, **Then** they see a progress indicator and the backup uploads regardless of WiFi/mobile data
5. **Given** 35 daily backups exist on Google Drive, **When** a new backup is uploaded, **Then** the oldest 6 backups are automatically deleted keeping only 30 days
6. **Given** the network fails during upload, **When** the error occurs, **Then** the sync shows "Failed" status with a "Retry" button and doesn't crash the app

---

### User Story 4 - Restore from Google Drive (Priority: P1 - High)

Users who lost or reset their device need to restore their complete IBS tracking history from Google Drive on a new device.

**Why this priority**: Completes the cloud backup story. Must ship with US3 to deliver actual disaster recovery value. Enables device migration and complete data recovery scenarios.

**Independent Test**: Install app on new device, sign in with Google account, select cloud backup, and verify all historical data is restored. Delivers complete cloud recovery workflow.

**Acceptance Scenarios**:

1. **Given** a user installs the app on a new device, **When** they open Settings and tap "Restore from cloud", **Then** they are prompted to sign in with Google if not already authenticated
2. **Given** a user is signed in to Google, **When** they tap "Restore from cloud", **Then** they see a list of up to 30 cloud backups sorted by date with file sizes
3. **Given** multiple backups are available, **When** the list displays, **Then** the most recent backup is highlighted or marked as "Latest"
4. **Given** a user selects a 7-day-old backup, **When** they tap Restore, **Then** the backup downloads with a progress indicator
5. **Given** the download completes, **When** the user confirms restore, **Then** their current database is replaced and they see "Restored 500 items" success message
6. **Given** the network fails during download, **When** the error occurs, **Then** the user sees an error message with "Retry" and "Select different backup" options

---

### User Story 5 - Backup Settings & Configuration (Priority: P2 - Medium)

Users need control over backup behavior to manage storage, battery usage, and privacy preferences according to their individual needs.

**Why this priority**: Enables user customization and addresses edge cases. Less critical than core backup/restore functionality but important for user autonomy and troubleshooting.

**Independent Test**: Toggle backup settings off, verify backups stop, toggle on, verify backups resume. Delivers user control over backup behavior.

**Acceptance Scenarios**:

1. **Given** a user opens Settings, **When** they toggle "Enable local backups" to OFF, **Then** no automatic backups are created when they add food items
2. **Given** a user has "Enable Google Drive sync" toggled OFF, **When** 2:00 AM arrives, **Then** the scheduled sync task doesn't run
3. **Given** a user has backups disabled, **When** they tap "Backup now", **Then** a manual backup is created anyway (user-initiated action overrides toggle)
4. **Given** a user opens Settings, **When** they view the Backup Settings section, **Then** they see storage usage: "14 MB (local)" and "52 MB (cloud)"
5. **Given** a user taps "Clear all local backups", **When** they confirm the action, **Then** all local backup files are deleted and storage shows "0 MB (local)"
6. **Given** a user taps "Sign out of Google Drive", **When** they confirm, **Then** cloud sync is disabled and the next scheduled sync doesn't run
7. **Given** a user changes backup settings, **When** they close and reopen the app, **Then** their settings are persisted correctly

---

### Edge Cases

**Local Backup Failures:**
- What happens when device storage is full? System returns STORAGE_FULL error, skips backup, and logs warning. Backup status in Settings shows error state.
- How does system handle backup directory not accessible? Logs error to system log and shows error state in Settings > Backup status
- What if database is locked during backup? System retries once after 100ms, then skips backup if still locked to avoid blocking user
- How does system detect corrupted backup files? Performs file integrity check when listing backups, marks corrupted files as invalid, excludes from restore list

**Google Drive Failures:**
- What happens when there's no internet connection? Sync is skipped, task reschedules for next day, no error shown to user (expected behavior)
- How does system handle expired Google Sign-In? Shows notification asking user to sign in again, disables automatic sync until re-authenticated
- What if upload times out? Retries with exponential backoff (3 attempts: immediate, +2s, +4s), then shows failed status with manual retry option
- How does system handle Drive quota exceeded? Shows error message suggesting clearing old backups, provides "Clear cloud backups" button
- What if download is corrupted? Verifies file integrity after download, shows error if corrupted, allows retry or selecting different backup

**Restore Failures:**
- What happens when backup file is corrupted? Shows error message "Backup file is corrupted and cannot be restored", prevents restore operation
- How does system handle database version mismatch? Detects schema version, shows error "Backup from incompatible app version", explains upgrade/downgrade issue
- What if restore is interrupted (app crash/force close)? On next app start, detects incomplete restore, rolls back to pre-restore state automatically
- How does system recover from crash during restore? Maintains pre-restore backup, auto-detects crash on startup, restores from pre-restore backup

**Performance Edge Cases:**
- What happens with large databases (1000+ entries)? Backup creation tested up to 5MB database size, must complete in under 500ms
- How does system handle backup during active data entry? Waits for current save operation to complete, then creates backup immediately
- What if multiple changes happen rapidly? Debounce is NOT used - each change triggers immediate backup to ensure no data loss

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST automatically create a local backup after every food item is added, edited, or deleted
- **FR-002**: System MUST automatically create a local backup after every symptom is added, edited, or deleted
- **FR-003**: System MUST maintain exactly 7 most recent local backups and automatically delete older backups
- **FR-004**: System MUST complete local backup creation in under 200ms to avoid blocking user interactions
- **FR-005**: System MUST store local backups in app-specific storage that persists across app updates but is deleted on uninstall
- **FR-006**: System MUST provide a Settings screen with a "Backup & Restore" section showing backup status
- **FR-007**: System MUST display the timestamp of the last local backup in human-readable relative format (e.g., "2 minutes ago", "1 hour ago")
- **FR-008**: System MUST display the count of available local backups (e.g., "7 backups available")
- **FR-009**: System MUST provide a "Restore from local backup" feature that displays a list of all available backups sorted by timestamp (newest first)
- **FR-010**: System MUST display each backup with creation date/time in human-readable format and file size in MB
- **FR-011**: System MUST show a confirmation dialog before restoring that warns current data will be replaced
- **FR-012**: System MUST create a safety backup of current data before performing any restore operation
- **FR-013**: System MUST display a success message after restore showing count of restored items
- **FR-014**: System MUST refresh all UI screens automatically after restore completes
- **FR-015**: System MUST schedule a daily backup task to run at 2:00 AM using platform scheduling
- **FR-016**: System MUST only run scheduled cloud sync when device is connected to WiFi
- **FR-017**: System MUST only run scheduled cloud sync when device battery is not low
- **FR-018**: System MUST only run scheduled cloud sync when device is idle
- **FR-019**: System MUST upload backups to Google Drive app folder (private, not visible in user's Drive)
- **FR-020**: System MUST maintain exactly 30 most recent cloud backups and automatically delete older backups
- **FR-021**: System MUST display cloud sync status with states: Synced, Syncing, Failed, or Never
- **FR-022**: System MUST display last sync timestamp and next scheduled sync time
- **FR-023**: System MUST provide a "Backup now" button for manual immediate cloud sync
- **FR-024**: System MUST show progress indicator during manual cloud sync
- **FR-025**: System MUST handle sync failures gracefully without crashing and provide retry option
- **FR-026**: System MUST provide a "Restore from cloud" feature requiring Google Sign-In if not authenticated
- **FR-027**: System MUST display list of up to 30 cloud backups sorted by date with most recent highlighted
- **FR-028**: System MUST show download progress indicator when downloading cloud backup
- **FR-029**: System MUST verify file integrity after cloud download before allowing restore
- **FR-030**: System MUST provide settings toggles for "Enable local backups" and "Enable Google Drive sync" (both default ON)
- **FR-031**: System MUST allow manual "Backup now" to work even when toggles are disabled
- **FR-032**: System MUST display backup storage usage in MB for both local and cloud
- **FR-033**: System MUST provide a "Clear all local backups" button with confirmation dialog
- **FR-034**: System MUST provide a "Sign out of Google Drive" button that disables cloud sync
- **FR-035**: System MUST persist all backup settings across app restarts

### Key Entities

- **Backup File**: Represents a snapshot of the database at a specific point in time. Attributes include timestamp (Unix epoch), file size in bytes, location (local or cloud), and status (available, uploading, downloading, failed, corrupted).

- **Backup Metadata**: Information about a backup without the file itself. Includes creation timestamp, human-readable date/time, file name, file size in MB, storage location, and validation status.

- **Sync Status**: Represents the current state of cloud synchronization. Attributes include status enum (Synced/Syncing/Failed/Never), last sync timestamp, next scheduled sync timestamp, and error message if failed.

- **Backup Settings**: User preferences for backup behavior. Includes local backups enabled (boolean), cloud sync enabled (boolean), last backup timestamp, sync status, storage usage metrics, and Google account authentication state.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Zero user reports of permanent data loss after feature deployment
- **SC-002**: Local backup creation completes in under 200ms average across all devices
- **SC-003**: Users can restore from local backup in under 3 seconds from clicking restore to seeing restored data
- **SC-004**: 99% of local backups complete successfully without errors
- **SC-005**: 95% of scheduled cloud syncs complete successfully (allowing 5% for expected network issues)
- **SC-006**: Cloud backup upload completes in under 10 seconds for typical 2MB database
- **SC-007**: Cloud backup download completes in under 10 seconds for typical 2MB database
- **SC-008**: No user-reported app slowdowns or performance degradation due to backup operations
- **SC-009**: Battery impact from daily cloud sync is under 1% per day
- **SC-010**: 80% of users keep local backups enabled (default ON setting)
- **SC-011**: 60% of users keep Google Drive sync enabled
- **SC-012**: At least 10% of users successfully use the restore feature within first 3 months
- **SC-013**: User satisfaction with backup feature measured at 4.5+ out of 5 stars
- **SC-014**: Support tickets related to data loss reduced by 90% after deployment
- **SC-015**: Local backup storage usage remains under 15MB for 7 backups
- **SC-016**: Cloud backup storage usage remains under 65MB for 30 backups

### Dependencies

- **Existing Infrastructure**: Room Database already in use for data storage (version 2.6.1)
- **Existing Infrastructure**: Kotlin Coroutines already in use for asynchronous operations
- **Existing Infrastructure**: Jetpack Compose already in use for UI layer
- **External Service**: Google Drive API for cloud backup functionality
- **External Service**: Google Sign-In for user authentication with Google account
- **Platform Service**: Android WorkManager for scheduled background tasks
- **Platform Permission**: Internet permission already granted for Google Drive access
- **Platform Permission**: No additional runtime permissions required (app-specific storage)

### Assumptions

- **Assumption**: Average database size is 2MB for typical usage (up to 1000 food/symptom entries)
- **Assumption**: Users have Google accounts for cloud backup (Android devices come with Google account by default)
- **Assumption**: Users have at least 100MB free storage on device for local backups and temporary operations
- **Assumption**: WiFi is available daily for most users for scheduled cloud sync
- **Assumption**: Google Drive app folder storage is sufficient (60MB for 30 backups is well under free tier limits)
- **Assumption**: Users understand "backup" and "restore" terminology without additional onboarding
- **Assumption**: Room database file can be safely copied while database is open (SQLite supports this)
- **Assumption**: Users will not attempt to manually modify backup files in file system
- **Assumption**: Cloud sync failures are primarily due to network issues, not service outages

### Out of Scope

The following are explicitly not included in this feature and may be considered for future releases:

- **Export as ZIP**: Exporting backup files as downloadable ZIP archives for manual sharing
- **Import from file**: Importing backup files from device file picker or external storage
- **Backup encryption**: Password-protected or encrypted backup files for enhanced security
- **Auto-restore on crash**: Automatic detection and restoration when app crash is detected
- **Real-time multi-device sync**: Synchronizing data in real-time across multiple devices
- **Alternative cloud providers**: Support for Dropbox, OneDrive, or other cloud storage services
- **Selective restore**: Restoring only specific data types (e.g., only foods, only symptoms)
- **Backup analytics**: Visualizing data growth trends over time from backup history
- **Backup scheduling options**: Allowing users to customize sync time beyond 2:00 AM
- **Backup retention customization**: Allowing users to configure number of backups kept (fixed at 7 local, 30 cloud)

### Known Limitations

**Scheduling Precision**:
- WorkManager uses a 1-hour flex period (1:00-2:00 AM execution window) due to Android battery optimization
- Exact 2:00 AM execution cannot be guaranteed - this is a platform limitation, not a bug
- Actual execution time depends on device state (charging status, WiFi availability, system idle detection)
- On Android 6.0+ with Doze mode, execution may be delayed if device is in deep sleep

**Idle State Detection**:
- WorkManager relies on Android's system idle detection for scheduled tasks
- Behavior varies across device manufacturers (Samsung, Xiaomi, OnePlus may have aggressive battery policies)
- Users with third-party battery optimization apps may experience delayed or skipped sync
- Recommendation: Users should exclude app from battery optimization for reliable scheduled sync

**Workaround for Scheduling Constraints**:
- Users can manually trigger "Backup now" from Settings, which bypasses all constraints (WiFi, charging, idle)
- Manual backup works immediately regardless of device state or time of day
- This provides guaranteed backup capability even if scheduled sync is delayed
