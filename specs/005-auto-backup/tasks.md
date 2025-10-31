# Tasks: Automatic Backup System

**Input**: Design documents from `/specs/005-auto-backup/`
**Prerequisites**: plan.md (complete), spec.md (complete), research.md (complete), data-model.md (complete), contracts/ (complete)

**Tests**: Tests are included in this implementation to ensure data integrity and backup reliability.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Android project**: `app/src/main/java/com/tiarkaerell/ibstracker/`
- **Tests**: `app/src/androidTest/java/com/tiarkaerell/ibstracker/`
- Paths follow existing project structure from CLAUDE.md

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add dependencies and create basic project structure for backup system

- [X] T001 Add WorkManager dependency (androidx.work:work-runtime-ktx:2.9.0) to app/build.gradle.kts
- [X] T002 Add Google Play Services Auth dependency (com.google.android.gms:play-services-auth:20.7.0) to app/build.gradle.kts
- [X] T003 [P] Add Google API Client dependencies (google-api-client-android:2.2.0, google-http-client-gson:1.43.3) to app/build.gradle.kts
- [X] T004 [P] Add Google Drive API v3 dependency (google-api-services-drive:v3-rev20230822-2.0.0) to app/build.gradle.kts
- [X] T005 [P] Add Kotlin Coroutines Test dependency (kotlinx-coroutines-test:1.8.0) to app/build.gradle.kts
- [X] T006 Sync Gradle project and verify all dependencies resolve
- [X] T007 [P] Create backup package structure: app/src/main/java/com/tiarkaerell/ibstracker/data/backup/
- [X] T008 [P] Create backup models package: app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/
- [X] T009 [P] Create backup preferences package: app/src/main/java/com/tiarkaerell/ibstracker/data/preferences/
- [X] T010 [P] Create backup test package: app/src/androidTest/java/com/tiarkaerell/ibstracker/backup/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data models and infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T011 [P] Create BackupLocation enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/BackupLocation.kt
- [X] T012 [P] Create BackupStatus enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/BackupStatus.kt
- [X] T013 Create BackupFile data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/BackupFile.kt
- [X] T014 [P] Create BackupError enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/BackupError.kt
- [X] T015 [P] Create BackupResult sealed class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/BackupResult.kt
- [X] T016 [P] Create RestoreError enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/RestoreError.kt
- [X] T017 [P] Create RestoreResult sealed class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/RestoreResult.kt
- [X] T018 [P] Create SyncState enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/SyncState.kt
- [X] T019 Create SyncStatus data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/SyncStatus.kt
- [X] T020 Create BackupSettings data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/BackupSettings.kt
- [X] T021 [P] Create BackupMetadata data class with toMetadata() extension in app/src/main/java/com/tiarkaerell/ibstracker/data/model/backup/BackupMetadata.kt
- [X] T022 Create BackupPreferences DataStore repository in app/src/main/java/com/tiarkaerell/ibstracker/data/preferences/BackupPreferences.kt
- [X] T023 [P] Create BackupFileManager utility for file operations (copy with checksum) in app/src/main/java/com/tiarkaerell/ibstracker/data/backup/BackupFileManager.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Local Backup After Changes (Priority: P0 - Critical) 🎯 MVP

**Goal**: System automatically creates a local backup after every data change (food/symptom add/edit/delete) in under 200ms

**Independent Test**: Add/edit/delete a food item or symptom, verify backup file exists in local storage with current timestamp and passes checksum verification

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T024 [P] [US1] Create BackupManagerTest with setup/teardown in app/src/androidTest/java/com/tiarkaerell/ibstracker/backup/BackupManagerTest.kt
- [X] T025 [P] [US1] Add test: testCreateLocalBackup_success() verifies backup created in <200ms
- [X] T026 [P] [US1] Add test: testBackupChecksum_integrity() verifies SHA-256 checksum file created
- [X] T027 [P] [US1] Add test: testCleanupOldBackups_keepsSevenMostRecent() verifies retention policy
- [X] T028 [P] [US1] Add test: testWALCheckpoint_executed() verifies PRAGMA wal_checkpoint(FULL) runs
- [X] T029 [P] [US1] Add test: testStorageFull_returnsFailure() verifies error handling
- [X] T030 Run BackupManagerTest - verify all tests FAIL (expected before implementation)

### Implementation for User Story 1

- [X] T031 [US1] Implement BackupManager class with createLocalBackup() method in app/src/main/java/com/tiarkaerell/ibstracker/data/backup/BackupManager.kt
- [X] T032 [US1] Add hasEnoughStorageSpace() method to BackupManager
- [X] T033 [US1] Add listLocalBackups() Flow method to BackupManager
- [X] T034 [US1] Add deleteLocalBackup() method to BackupManager
- [X] T035 [US1] Add deleteAllLocalBackups() method to BackupManager
- [X] T036 [US1] Add calculateLocalStorageUsage() method to BackupManager
- [X] T037 [US1] Add verifyBackupIntegrity() method to BackupManager
- [X] T038 [US1] Add cleanupOldBackups() private method to BackupManager
- [X] T039 [US1] Update AppContainer to instantiate BackupManager in app/src/main/java/com/tiarkaerell/ibstracker/AppContainer.kt
- [X] T040 [US1] Update DataRepository constructor to accept BackupManager parameter in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/DataRepository.kt
- [X] T041 [US1] Add backup trigger to insertFoodItem() in DataRepository
- [X] T042 [US1] Add backup trigger to updateFoodItem() in DataRepository
- [X] T043 [US1] Add backup trigger to deleteFoodItem() in DataRepository
- [X] T044 [US1] Add backup trigger to insertSymptom() in DataRepository
- [X] T045 [US1] Add backup trigger to updateSymptom() in DataRepository
- [X] T046 [US1] Add backup trigger to deleteSymptom() in DataRepository
- [X] T047 [US1] Run BackupManagerTest - verify all tests PASS
- [ ] T048 [US1] Manual test: Add food item, verify backup file created in app/data/data/com.tiarkaerell.ibstracker/files/ (USER TASK)
- [ ] T049 [US1] Manual test: Add 10 food items, verify only 7 backup files remain (USER TASK)

**Checkpoint**: User Story 1 complete - local backups working automatically after data changes

---

## Phase 4: User Story 2 - Restore from Local Backup (Priority: P0 - Critical)

**Goal**: Users can restore their data from a recent local backup through the Settings UI

**Independent Test**: Create a backup with known data, delete all current data, use restore feature to recover, verify all data returns correctly

### Tests for User Story 2

- [X] T050 [P] [US2] Create RestoreManagerTest with setup/teardown in app/src/androidTest/java/com/tiarkaerell/ibstracker/backup/RestoreManagerTest.kt
- [X] T051 [P] [US2] Add test: testRestoreFromBackup_success() verifies data restored correctly
- [X] T052 [P] [US2] Add test: testRestoreFromBackup_checksumMismatch() verifies corrupted backup detection
- [X] T053 [P] [US2] Add test: testRestoreFromBackup_versionMismatch() verifies incompatible version error
- [X] T054 [P] [US2] Add test: testRestoreFromBackup_createsPreRestoreBackup() verifies safety backup created
- [X] T055 [P] [US2] Create BackupIntegrationTest for full backup→restore flow in app/src/androidTest/java/com/tiarkaerell/ibstracker/backup/BackupIntegrationTest.kt
- [X] T056 [P] [US2] Add test: testFullBackupRestoreFlow() verifies end-to-end data recovery
- [X] T057 Run RestoreManagerTest and BackupIntegrationTest - verify all tests FAIL

### Implementation for User Story 2

- [X] T058 [US2] Implement RestoreManager class with restoreFromBackup() method in app/src/main/java/com/tiarkaerell/ibstracker/data/backup/RestoreManager.kt
- [X] T059 [US2] Add verifyBackupFile() method to RestoreManager
- [X] T060 [US2] Add checkDatabaseVersionCompatibility() method to RestoreManager
- [X] T061 [US2] Add createPreRestoreBackup() method to RestoreManager
- [X] T062 [US2] Add performDatabaseRestore() method to RestoreManager
- [X] T063 [US2] Add countRestoredItems() method to RestoreManager
- [X] T064 [US2] Create BackupRepository interface in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/BackupRepository.kt
- [X] T065 [US2] Implement BackupRepositoryImpl with observeSettings() in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/BackupRepositoryImpl.kt
- [X] T066 [US2] Add updateSettings() and toggle methods to BackupRepositoryImpl
- [X] T067 [US2] Add createLocalBackup() delegation to BackupRepositoryImpl
- [X] T068 [US2] Add observeLocalBackups() to BackupRepositoryImpl
- [X] T069 [US2] Add restoreFromBackup() delegation to BackupRepositoryImpl
- [X] T070 [US2] Add isBackupCompatible() to BackupRepositoryImpl
- [X] T071 [US2] Update AppContainer to instantiate BackupRepository in app/src/main/java/com/tiarkaerell/ibstracker/AppContainer.kt
- [X] T072 [US2] Create BackupViewModel with settings/backups flows in app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/BackupViewModel.kt
- [X] T073 [US2] Add createLocalBackup() and restoreBackup() methods to BackupViewModel
- [X] T074 [US2] Update ViewModelFactory to support BackupViewModel in app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/ViewModelFactory.kt
- [X] T075 [US2] Create BackupListItem composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/BackupListItem.kt
- [X] T076 [US2] Create BackupStatusCard composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/BackupStatusCard.kt
- [X] T077 [US2] Create BackupSettingsScreen composable with local backups section in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/BackupSettingsScreen.kt
- [X] T078 [US2] Add restore dialog with confirmation to BackupSettingsScreen
- [X] T079 [US2] Add backup list with timestamps and sizes to BackupSettingsScreen
- [X] T080 [US2] Update SettingsScreen to include "Backup & Restore" navigation item in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/SettingsScreen.kt
- [X] T081 [US2] Add navigation route for BackupSettingsScreen in MainActivity (update NavHost)
- [X] T082 [US2] Run RestoreManagerTest - verify all tests PASS
- [X] T083 [US2] Run BackupIntegrationTest - verify full flow works
- [ ] T084 [US2] Manual test: Create backup, delete all data, restore, verify data recovered (USER TASK)
- [ ] T085 [US2] Manual test: Restore shows "Restored X items" success message (USER TASK)

**Checkpoint**: User Stories 1 AND 2 complete - local backup and restore fully functional

---

## Phase 5: User Story 3 - Google Drive Daily Sync (Priority: P1 - High)

**Goal**: System automatically syncs database to Google Drive once daily at 2:00 AM when on WiFi and charging

**Independent Test**: Wait for scheduled 2:00 AM sync or trigger manual sync, verify file appears in Google Drive app folder with correct timestamp

### Tests for User Story 3

- [X] T086 [P] [US3] Create GoogleDriveBackupWorkerTest with WorkManager test setup in app/src/androidTest/java/com/tiarkaerell/ibstracker/backup/GoogleDriveBackupWorkerTest.kt
- [X] T087 [P] [US3] Add test: testBackupWorker_success() verifies worker completes successfully
- [X] T088 [P] [US3] Add test: testBackupWorker_constraints() verifies WiFi/charging requirements
- [X] T089 [P] [US3] Add test: testBackupWorker_syncDisabled() verifies respects settings toggle
- [X] T090 [P] [US3] Add test: testBackupWorker_notSignedIn() verifies requires authentication
- [X] T091 [P] [US3] Add test: testBackupWorker_retryOnNetworkFailure() verifies exponential backoff
- [X] T092 Run GoogleDriveBackupWorkerTest - verify all tests FAIL

### Implementation for User Story 3

- [X] T093 [US3] Create GoogleDriveService helper class for Drive API operations in app/src/main/java/com/tiarkaerell/ibstracker/data/backup/GoogleDriveService.kt
- [X] T094 [US3] Add initializeDriveService() method to GoogleDriveService
- [X] T095 [US3] Add uploadBackupToDrive() method to GoogleDriveService
- [X] T096 [US3] Add listCloudBackups() method to GoogleDriveService
- [X] T097 [US3] Add downloadBackupFromDrive() method to GoogleDriveService
- [X] T098 [US3] Add deleteCloudBackup() method to GoogleDriveService
- [X] T099 [US3] Add cleanupOldCloudBackups() method to GoogleDriveService
- [X] T100 [US3] Implement GoogleDriveBackupWorker with doWork() in app/src/main/java/com/tiarkaerell/ibstracker/data/backup/GoogleDriveBackupWorker.kt
- [X] T101 [US3] Add constraint checking to GoogleDriveBackupWorker
- [X] T102 [US3] Add error handling with retry logic to GoogleDriveBackupWorker
- [X] T103 [US3] Add sync status updates to GoogleDriveBackupWorker
- [X] T104 [US3] Add scheduleCloudBackup() method to BackupRepositoryImpl
- [X] T105 [US3] Add cancelCloudBackup() method to BackupRepositoryImpl
- [X] T106 [US3] Add triggerManualCloudSync() method to BackupRepositoryImpl
- [X] T107 [US3] Add observeSyncStatus() Flow to BackupRepositoryImpl
- [X] T108 [US3] Add observeCloudBackups() Flow to BackupRepositoryImpl
- [X] T109 [US3] Update IBSTrackerApplication.onCreate() to call scheduleCloudBackup() in app/src/main/java/com/tiarkaerell/ibstracker/IBSTrackerApplication.kt
- [X] T110 [US3] Add Google Sign-In button to BackupSettingsScreen
- [X] T111 [US3] Add cloud sync toggle to BackupSettingsScreen
- [X] T112 [US3] Add sync status display (Synced/Syncing/Failed/Never) to BackupSettingsScreen
- [X] T113 [US3] Add "Backup now" button with progress indicator to BackupSettingsScreen
- [X] T114 [US3] Add last sync timestamp display to BackupSettingsScreen
- [X] T115 [US3] Add next sync timestamp display to BackupSettingsScreen
- [X] T116 [US3] Run GoogleDriveBackupWorkerTest - verify all tests PASS
- [ ] T117 [US3] Manual test: Sign in to Google, enable sync, tap "Backup now" (USER TASK)
- [ ] T118 [US3] Manual test: Check WorkManager logs (adb logcat -s WM-WorkerWrapper) (USER TASK)
- [ ] T119 [US3] Manual test: Verify backup appears in Google Drive appDataFolder (API query) (USER TASK)

**Checkpoint**: User Stories 1, 2, AND 3 complete - local and cloud backup working

---

## Phase 6: User Story 4 - Restore from Google Drive (Priority: P1 - High)

**Goal**: Users can restore complete IBS tracking history from Google Drive on a new device

**Independent Test**: Install app on new device, sign in with Google account, select cloud backup, verify all historical data is restored

### Tests for User Story 4

- [X] T120 [P] [US4] Add test: testRestoreFromCloud_success() to RestoreManagerTest
- [X] T121 [P] [US4] Add test: testRestoreFromCloud_downloadFailure() to RestoreManagerTest
- [X] T122 [P] [US4] Add test: testRestoreFromCloud_checksumMismatch() to RestoreManagerTest
- [X] T123 [P] [US4] Add test: testCloudBackupList_sortedByDate() to BackupIntegrationTest
- [X] T124 Run updated tests - verify all tests FAIL

### Implementation for User Story 4

- [X] T125 [US4] Add downloadBackup() method to RestoreManager for cloud backups
- [X] T126 [US4] Update restoreFromBackup() to handle CLOUD location in RestoreManager
- [X] T127 [US4] Add download progress tracking to RestoreManager
- [X] T128 [US4] Add signInToGoogle() method to BackupRepositoryImpl
- [X] T129 [US4] Add signOutOfGoogle() method to BackupRepositoryImpl
- [X] T130 [US4] Add isGoogleSignedIn() method to BackupRepositoryImpl
- [ ] T131 [US4] Update BackupViewModel to handle Google Sign-In flow
- [ ] T132 [US4] Add restoreFromCloud() method to BackupViewModel
- [ ] T133 [US4] Create GoogleSignInActivity for OAuth flow in app/src/main/java/com/tiarkaerell/ibstracker/ui/GoogleSignInActivity.kt
- [ ] T134 [US4] Add "Restore from cloud" button to BackupSettingsScreen
- [ ] T135 [US4] Add cloud backup list with download progress to BackupSettingsScreen
- [ ] T136 [US4] Add cloud backup selection dialog to BackupSettingsScreen
- [ ] T137 [US4] Add "Latest" badge highlighting to most recent cloud backup
- [ ] T138 [US4] Add restore confirmation dialog for cloud backups
- [ ] T139 [US4] Add network error handling with retry option
- [ ] T140 [US4] Run updated RestoreManagerTest - verify cloud tests PASS
- [ ] T141 [US4] Run BackupIntegrationTest - verify cloud restore flow works
- [ ] T142 [US4] Manual test: Restore from cloud backup, verify data recovered
- [ ] T143 [US4] Manual test: Network failure during download shows retry option

**Checkpoint**: All critical and high-priority user stories complete - full backup/restore system functional

---

## Phase 7: User Story 5 - Backup Settings & Configuration (Priority: P2 - Medium)

**Goal**: Users have control over backup behavior, storage management, and privacy preferences

**Independent Test**: Toggle backup settings off, verify backups stop, toggle on, verify backups resume

### Tests for User Story 5

- [ ] T144 [P] [US5] Create BackupPreferencesTest in app/src/androidTest/java/com/tiarkaerell/ibstracker/backup/BackupPreferencesTest.kt
- [ ] T145 [P] [US5] Add test: testToggleLocalBackups_respectsSetting() to BackupManagerTest
- [ ] T146 [P] [US5] Add test: testToggleCloudSync_respectsSetting() to GoogleDriveBackupWorkerTest
- [ ] T147 [P] [US5] Add test: testManualBackup_overridesToggle() to BackupManagerTest
- [ ] T148 [P] [US5] Add test: testStorageUsage_calculation() to BackupIntegrationTest
- [ ] T149 Run preference tests - verify all tests FAIL

### Implementation for User Story 5

- [ ] T150 [US5] Add calculateStorageUsage() method to BackupRepositoryImpl
- [ ] T151 [US5] Add observeStorageUsage() Flow to BackupRepositoryImpl
- [ ] T152 [US5] Update BackupManager.createLocalBackup() to check localBackupsEnabled setting
- [ ] T153 [US5] Update GoogleDriveBackupWorker to check cloudSyncEnabled setting
- [ ] T154 [US5] Add storage usage section to BackupSettingsScreen (local MB, cloud MB)
- [ ] T155 [US5] Add "Clear all local backups" button with confirmation dialog
- [ ] T156 [US5] Add "Sign out of Google Drive" button to BackupSettingsScreen
- [ ] T157 [US5] Add settings persistence validation on app restart
- [ ] T158 [US5] Run BackupPreferencesTest - verify all tests PASS
- [ ] T159 [US5] Manual test: Toggle local backups OFF, add food, verify no backup created
- [ ] T160 [US5] Manual test: Tap "Backup now" with toggle OFF, verify backup created
- [ ] T161 [US5] Manual test: Clear all local backups, verify storage shows "0 MB (local)"
- [ ] T162 [US5] Manual test: Sign out of Google, verify sync disabled

**Checkpoint**: All user stories complete - full backup system with user controls

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements, documentation, and validation

- [X] T163 [P] Update CLAUDE.md with backup feature technologies (WorkManager, Google Drive API)
- [X] T164 [P] Create feature documentation in docs/features/auto-backup-system.md
- [X] T165 [P] Add inline code documentation to BackupManager
- [X] T166 [P] Add inline code documentation to RestoreManager
- [X] T167 [P] Add inline code documentation to GoogleDriveService
- [X] T168 Code cleanup: Remove debug logs and commented code
- [X] T169 Code cleanup: Verify all TODOs resolved or documented
- [ ] T170 Performance validation: Run profiler, verify local backup <200ms (USER TASK)
- [ ] T171 Performance validation: Verify restore <3 seconds (USER TASK)
- [ ] T172 Performance validation: Verify cloud upload <10 seconds on WiFi (USER TASK)
- [ ] T173 [P] Security review: Verify DRIVE_APPDATA scope (not full DRIVE) (USER TASK)
- [ ] T174 [P] Security review: Verify checksum verification on all restores (USER TASK)
- [ ] T175 [P] Security review: Verify WAL checkpoint before all backups (USER TASK)
- [X] T176 Run complete test suite: ./gradlew test connectedAndroidTest
- [X] T177 Verify all 0 test failures
- [ ] T178 Run quickstart.md manual testing scenarios (Steps 9.1, 9.2, 9.3) (USER TASK)
- [ ] T179 Verify backup files cleanup (only 7 local, 30 cloud) (USER TASK)
- [ ] T180 Final validation: Complete backup→modify→restore→verify cycle (USER TASK)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational completion - MVP start
- **User Story 2 (Phase 4)**: Depends on Foundational + US1 (needs BackupManager)
- **User Story 3 (Phase 5)**: Depends on Foundational + US1 (needs local backup working)
- **User Story 4 (Phase 6)**: Depends on Foundational + US2 + US3 (needs restore + cloud)
- **User Story 5 (Phase 7)**: Depends on all previous stories (adds controls)
- **Polish (Phase 8)**: Depends on all user stories being complete

### User Story Independence

- **User Story 1 (P0)**: Local backup - Core MVP, required by all others
- **User Story 2 (P0)**: Restore - Depends on US1, completes local story
- **User Story 3 (P1)**: Cloud sync - Depends on US1, independent of US2
- **User Story 4 (P1)**: Cloud restore - Depends on US2+US3, completes cloud story
- **User Story 5 (P2)**: Settings - Depends on all previous, adds user controls

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Data models before services
- Services before UI components
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- **Phase 1 Setup**: All tasks except T006 can run in parallel
- **Phase 2 Foundational**: All [P] tasks (T011-T023) can run in parallel
- **Phase 3 US1 Tests**: All test tasks (T024-T029) can run in parallel
- **Phase 5 US3 Tests**: All test tasks (T086-T091) can run in parallel
- **Phase 8 Polish**: All [P] documentation tasks can run in parallel

---

## Parallel Example: User Story 1 Tests

```bash
# Launch all US1 tests together:
Task: "Create BackupManagerTest with setup/teardown"
Task: "Add test: testCreateLocalBackup_success() verifies backup created in <200ms"
Task: "Add test: testBackupChecksum_integrity() verifies SHA-256 checksum file created"
Task: "Add test: testCleanupOldBackups_keepsSevenMostRecent() verifies retention policy"
Task: "Add test: testWALCheckpoint_executed() verifies PRAGMA wal_checkpoint(FULL) runs"
Task: "Add test: testStorageFull_returnsFailure() verifies error handling"
```

---

## Implementation Strategy

### MVP First (User Stories 1 + 2 Only)

1. Complete Phase 1: Setup (T001-T010)
2. Complete Phase 2: Foundational (T011-T023) - CRITICAL blocker
3. Complete Phase 3: User Story 1 - Local Backup (T024-T049)
4. Complete Phase 4: User Story 2 - Local Restore (T050-T085)
5. **STOP and VALIDATE**: Test local backup/restore independently
6. Deploy/demo if ready (local protection working!)

### Incremental Delivery

1. **Sprint 1**: Setup + Foundational + US1 → Local backup working
2. **Sprint 2**: US2 → Local restore working → **MVP COMPLETE**
3. **Sprint 3**: US3 → Cloud sync working
4. **Sprint 4**: US4 → Cloud restore working
5. **Sprint 5**: US5 + Polish → Complete feature with settings

### Parallel Team Strategy

With multiple developers:

1. **Week 1**: Team completes Setup + Foundational together (T001-T023)
2. **Week 2**:
   - Developer A: US1 (T024-T049) - Local backup
   - Developer B: US2 tests (T050-T057) - Prepare for restore
3. **Week 3**:
   - Developer A: US2 implementation (T058-T085) - Restore UI
   - Developer B: US3 tests (T086-T092) - Prepare for cloud
4. **Week 4**:
   - Developer A: US3 (T093-T119) - Cloud sync
   - Developer B: US4 (T120-T143) - Cloud restore
5. **Week 5**:
   - Developer A: US5 (T144-T162) - Settings
   - Developer B: Polish (T163-T180) - Documentation & validation

---

## Success Criteria Validation

After completing all phases, verify:

- ✅ SC-001: Zero data loss in testing (backup/restore cycle preserves all data)
- ✅ SC-002: Local backup <200ms average (run profiler on T170)
- ✅ SC-003: Restore <3 seconds (run profiler on T171)
- ✅ SC-004: 99% local backup success rate (run integration tests 100 times)
- ✅ SC-005: 95% cloud sync success rate (allow 5% for network issues)
- ✅ SC-006: Cloud upload <10 seconds for 2MB database (run profiler on T172)
- ✅ SC-007: Cloud download <10 seconds for 2MB database (test on T172)
- ✅ SC-008: No performance degradation (profile app before/after feature)
- ✅ SC-015: Local storage <15MB for 7 backups (verify on T179)
- ✅ SC-016: Cloud storage <65MB for 30 backups (verify on T179)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Tests written FIRST, implementation SECOND (TDD approach)
- Commit after each logical group of tasks
- Stop at any checkpoint to validate story independently
- Manual tests complement automated tests for UI/integration validation
- Android-specific: Use `adb` commands for file system verification
- WorkManager testing: Use WorkManagerTestInitHelper for unit tests
