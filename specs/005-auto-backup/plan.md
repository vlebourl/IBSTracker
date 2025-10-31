# Implementation Plan: Automatic Backup System

**Branch**: `005-auto-backup` | **Date**: 2025-10-27 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/005-auto-backup/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Implement a two-tier automatic backup system for IBS Tracker to protect user data against accidental deletion, app crashes, and device loss. The system creates immediate local backups after every data change (food/symptom add/edit/delete) and performs daily Google Drive cloud sync at 2:00 AM. Users can restore from either local or cloud backups through a Settings UI.

**Technical Approach**: Extend existing Room database infrastructure with backup management layer, use WorkManager for scheduled cloud sync, integrate Google Drive API for cloud storage, and add Settings screens for backup configuration and restore functionality.

## Technical Context

**Language/Version**: Kotlin 1.8.20 / Android SDK 34 (Target SDK 34, Min SDK 26)
**Primary Dependencies**: Room 2.6.1, WorkManager 2.9+, Google Drive API v3, Google Sign-In, Jetpack Compose, Material3, Kotlin Coroutines
**Storage**: Room Database (SQLite) for app data, app-specific storage for local backups, Google Drive app folder for cloud backups
**Testing**: AndroidJUnit4, Room Testing, Coroutine Test (existing infrastructure), instrumented tests for backup/restore flows
**Target Platform**: Android 8.0+ (API 26+), optimized for Android 14 (API 34)
**Project Type**: Mobile (Android)
**Performance Goals**: Local backup <200ms, restore <3s, cloud upload/download <10s for 2MB database, 99% local backup success rate, 95% cloud sync success rate
**Constraints**: <1% battery impact per day, <15MB local storage (7 backups), <65MB cloud storage (30 backups), WiFi-only scheduled sync, device idle state required
**Scale/Scope**: Up to 1000 food/symptom entries (~2MB database), 7 local backups, 30 cloud backups, single-user per device

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Constitution Status**: Project constitution file is not yet ratified (template only). Proceeding with standard Android development best practices.

**Applied Standards**:
- ✅ **TDD Approach**: Write tests first for all backup/restore operations before implementation
- ✅ **Single Responsibility**: Each component has clear purpose (BackupManager, GoogleDriveBackupWorker, BackupRepository)
- ✅ **Integration Testing**: Required for backup/restore workflows, Google Drive sync, WorkManager scheduling
- ✅ **Clean Architecture**: Data layer (repositories, database), Domain layer (backup models, services), UI layer (composables, viewmodels)
- ✅ **Observability**: Structured logging for backup operations, error states exposed to UI, status tracking throughout

**No Constitution Violations**: Standard Android feature implementation following existing project patterns.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/tiarkaerell/ibstracker/
├── data/
│   ├── model/
│   │   ├── backup/
│   │   │   ├── BackupFile.kt          # NEW: Backup file entity
│   │   │   ├── BackupMetadata.kt      # NEW: Backup metadata model
│   │   │   ├── SyncStatus.kt          # NEW: Cloud sync status enum
│   │   │   └── BackupSettings.kt      # NEW: User backup preferences
│   ├── database/
│   │   ├── dao/
│   │   │   └── BackupDao.kt           # NEW: Backup metadata DAO (if persisted)
│   │   └── AppDatabase.kt             # MODIFIED: Add BackupFile entity (optional)
│   ├── repository/
│   │   ├── BackupRepository.kt        # NEW: Backup operations repository
│   │   └── DataRepository.kt          # MODIFIED: Add backup triggers
│   ├── backup/
│   │   ├── BackupManager.kt           # NEW: Core backup logic
│   │   ├── RestoreManager.kt          # NEW: Core restore logic
│   │   ├── GoogleDriveBackupWorker.kt # NEW: WorkManager for cloud sync
│   │   └── BackupFileManager.kt       # NEW: File operations (copy, delete, verify)
│   └── preferences/
│       └── BackupPreferences.kt       # NEW: DataStore for backup settings
│
├── ui/
│   ├── screens/
│   │   ├── BackupSettingsScreen.kt    # NEW: Backup & Restore settings screen
│   │   └── SettingsScreen.kt          # MODIFIED: Add Backup & Restore section
│   ├── viewmodel/
│   │   ├── BackupViewModel.kt         # NEW: Backup/restore state management
│   │   └── ViewModelFactory.kt        # MODIFIED: Add BackupViewModel
│   └── components/
│       └── BackupStatusCard.kt        # NEW: Backup status display component
│
└── AppContainer.kt                    # MODIFIED: Add BackupRepository

app/src/androidTest/java/com/tiarkaerell/ibstracker/
├── backup/
│   ├── BackupManagerTest.kt           # NEW: Local backup tests
│   ├── RestoreManagerTest.kt          # NEW: Restore flow tests
│   ├── GoogleDriveBackupWorkerTest.kt # NEW: Cloud sync tests
│   └── BackupIntegrationTest.kt       # NEW: End-to-end backup/restore tests
```

**Structure Decision**: Android mobile app structure following existing clean architecture pattern:
- **Data Layer** (`data/`): New `backup/` module for backup logic, new models in `model/backup/`, repository pattern
- **UI Layer** (`ui/`): New settings screen, new viewmodel, reusable components
- **Testing** (`androidTest/`): Comprehensive instrumented tests for all backup/restore operations
- **Dependencies**: WorkManager for scheduling, Google Drive API for cloud storage, DataStore for settings persistence

## Complexity Tracking

**No Violations Detected**: This feature follows standard Android architecture patterns and integrates cleanly with existing infrastructure. No complexity justification required.

---

## Phase 0: Research (COMPLETE)

**Status**: ✅ Complete
**Output**: [research.md](research.md)

**Key Decisions**:
1. **Room Backup**: WAL checkpoint + file copy with SHA-256 checksum
2. **Scheduling**: WorkManager PeriodicWorkRequest with 24h interval + 1h flex period
3. **Cloud Storage**: Google Drive API v3 with appDataFolder scope
4. **File I/O**: 8KB buffered streams for optimal performance
5. **Error Handling**: Categorized as transient (retry) vs permanent (fail)

**Dependencies Added**:
- androidx.work:work-runtime-ktx:2.9.0
- com.google.android.gms:play-services-auth:20.7.0
- com.google.api-client:google-api-client-android:2.2.0
- com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0

All research questions resolved. No NEEDS CLARIFICATION items remaining.

---

## Phase 1: Design & Contracts (COMPLETE)

**Status**: ✅ Complete
**Outputs**:
- [data-model.md](data-model.md) - Complete data structures
- [contracts/](contracts/) - API interfaces for all components
- [quickstart.md](quickstart.md) - Developer implementation guide
- CLAUDE.md updated with new technologies

**Data Models Created**:
- `BackupFile` - Backup file entity with location, checksum, status
- `BackupMetadata` - Lightweight metadata for UI display
- `SyncStatus` - Cloud sync state tracking
- `BackupSettings` - User preferences (DataStore)
- `BackupResult` / `RestoreResult` - Operation result types

**API Contracts Created**:
- `BackupManager` - Local backup creation and management (7 methods)
- `RestoreManager` - Database restore operations (6 methods)
- `GoogleDriveBackupWorker` - Scheduled cloud sync worker (6 methods + 3 static)
- `BackupRepository` - Repository layer coordinator (20+ methods)

**Project Structure Finalized**:
```
app/src/main/java/com/tiarkaerell/ibstracker/
├── data/backup/               # NEW: Backup logic
├── data/model/backup/         # NEW: Backup models
├── data/repository/           # MODIFIED: Add BackupRepository
├── data/preferences/          # NEW: BackupPreferences (DataStore)
├── ui/screens/                # NEW: BackupSettingsScreen
├── ui/viewmodel/              # NEW: BackupViewModel
└── ui/components/             # NEW: BackupStatusCard
```

**Agent Context Updated**: CLAUDE.md now includes WorkManager, Google Drive API, and backup patterns.

---

## Phase 2: Task Breakdown (PENDING)

**Next Command**: `/speckit.tasks`

This command will generate [tasks.md](tasks.md) with atomic, dependency-ordered tasks for implementation.

Expected task categories:
1. **Foundation** (Data models, interfaces)
2. **Local Backup** (BackupManager implementation)
3. **Cloud Sync** (GoogleDriveBackupWorker implementation)
4. **UI Layer** (Settings screens, ViewModels)
5. **Integration** (Repository, AppContainer updates)
6. **Testing** (Unit tests, integration tests)

---

## Re-Evaluation: Constitution Check (Post-Design)

**Status**: ✅ PASS - No violations

**Architecture Confirmation**:
- ✅ Clean separation: Data → Domain → UI layers
- ✅ Single responsibility per component (BackupManager, RestoreManager, Worker)
- ✅ Testable interfaces with clear contracts
- ✅ TDD-ready: All operations return result types for easy testing
- ✅ Observability: Status tracking, error states, progress callbacks
- ✅ Integration points: Hooks into existing DataRepository for automatic backups

**No Design Changes Required**: Architecture remains consistent with project standards.
