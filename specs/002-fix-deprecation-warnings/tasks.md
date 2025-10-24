# Tasks: Fix Build Process Deprecation Warnings

**Input**: Design documents from `/specs/002-fix-deprecation-warnings/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, quickstart.md ✅

**Tests**: No test tasks included - this is a refactoring/migration task with existing test suite validation

**Organization**: Tasks are grouped by user story (priority-based) to enable independent implementation and incremental validation

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1-US5 by priority)
- Include exact file paths in descriptions

## Path Conventions

- **Android project**: `app/src/main/java/com/tiarkaerell/ibstracker/`
- All paths are relative to repository root

---

## Phase 1: Setup & Baseline

**Purpose**: Establish baseline metrics and prepare for migration

- [X] T001 Capture baseline build with deprecation warnings: `./gradlew clean build --warning-mode all 2>&1 | tee build-baseline.log`
- [X] T002 Count baseline deprecation warnings: `grep -i "deprecat" build-baseline.log | wc -l` (expected: ~30) - **Result: 32 unique warnings**
- [X] T003 Run baseline test suite: `./gradlew test connectedAndroidTest` (all must pass) - **Result: All tests passed in build**
- [X] T004 Document baseline build time for performance comparison - **Result: 53 seconds**

---

## Phase 2: User Story 1 - FoodCategory Display Name Migration (Priority: P1) 🎯

**Goal**: Replace all deprecated `FoodCategoryHelper.getDisplayName(context, category)` calls with direct property access `category.displayName` across 3 screens (15 occurrences)

**Independent Test**: Run `./gradlew compileDebugKotlin --warning-mode all` and verify zero deprecation warnings for FoodCategory.getDisplayName; launch app and verify food categories display correctly in Dashboard, Food Screen, and Analytics

### Implementation for User Story 1

- [X] T005 [P] [US1] Replace FoodCategoryHelper.getDisplayName with category.displayName in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalyticsScreen.kt (2 occurrences at lines 268, 339)
- [X] T006 [P] [US1] Replace FoodCategoryHelper.getDisplayName with category.displayName in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/DashboardScreen.kt (5 occurrences at lines 189, 216, 221, 542, 555)
- [X] T007 [P] [US1] Replace FoodCategoryHelper.getDisplayName with category.displayName in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/FoodScreen.kt (7 occurrences at lines 243, 426, 501, 507, 532, 676, 724)
- [X] T008 [US1] Remove unused FoodCategoryHelper import from all affected files
- [X] T009 [US1] Compile and verify no FoodCategory deprecation warnings: `./gradlew compileDebugKotlin --warning-mode all 2>&1 | grep -i "FoodCategoryHelper"` - **Result: BUILD SUCCESSFUL, 0 FoodCategory warnings**
- [X] T010 [US1] Manual testing: Launch app, navigate Dashboard/Food/Analytics screens, verify category names display correctly - **Result: ✅ All category displays working correctly on physical device**
- [X] T011 [US1] Commit changes: `git add -A && git commit -m "fix(ui): Replace deprecated FoodCategoryHelper.getDisplayName with direct property access (US1)"` - **Result: Committed 109aa40**

**Checkpoint**: 15 deprecation warnings eliminated; food category display verified across all screens

---

## Phase 3: User Story 2 - Compose UI Components Migration (Priority: P2)

**Goal**: Replace deprecated Compose Material3 components: Divider → HorizontalDivider (5 occurrences) and menuAnchor() → menuAnchor(type, enabled) (4 occurrences) across 2 screens

**Independent Test**: Run `./gradlew compileDebugKotlin --warning-mode all` and verify zero deprecation warnings for Divider and menuAnchor; launch app and verify UI dividers render identically and dropdown menus function correctly

### Part A: Divider → HorizontalDivider Migration

- [X] T012 [P] [US2] Replace Divider with HorizontalDivider in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/DashboardScreen.kt (2 occurrences at lines 497, 507) and update import
- [X] T013 [P] [US2] Replace Divider with HorizontalDivider in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/SettingsScreen.kt (3 occurrences at lines 702, 747, 775) and update import

### Part B: menuAnchor() Signature Migration

- [X] T014 [P] [US2] Update menuAnchor() to menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true) in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/DashboardScreen.kt (1 occurrence at line 204) and add import
- [X] T015 [P] [US2] Update menuAnchor() to menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true) in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/SettingsScreen.kt (3 occurrences at lines 136, 185, 725) and add import
- [X] T016 [US2] Compile and verify no Compose UI component deprecation warnings: `./gradlew compileDebugKotlin --warning-mode all 2>&1 | grep -i "Divider\|menuAnchor"` - **Result: BUILD SUCCESSFUL, 0 warnings**
- [X] T017 [US2] Visual regression testing: Launch app, verify dividers render with identical spacing/styling, test all dropdown menus open/close correctly - **Result: ✅ All visual/functional tests passed**
- [X] T018 [US2] Commit changes: `git add -A && git commit -m "fix(ui): Migrate to HorizontalDivider and updated menuAnchor signature (US2)"` - **Result: Committed e6d897a**

**Checkpoint**: 9 deprecation warnings eliminated; UI components render identically and function correctly

---

## Phase 4: User Story 3 - KeyboardOptions Constructor Migration (Priority: P3)

**Goal**: Update KeyboardOptions constructor from deprecated `autoCorrect` parameter to `autoCorrectEnabled` in SettingsScreen password fields (4 occurrences)

**Independent Test**: Run `./gradlew compileDebugKotlin --warning-mode all` and verify zero deprecation warnings for KeyboardOptions constructor; test password fields in Settings to verify autocorrect disabled

### Implementation for User Story 3

- [ ] T019 [US3] Replace KeyboardOptions autoCorrect parameter with autoCorrectEnabled in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/SettingsScreen.kt (4 occurrences at lines 1301, 1327, 1417, 1500)
- [ ] T020 [US3] Compile and verify no KeyboardOptions deprecation warnings: `./gradlew compileDebugKotlin --warning-mode all 2>&1 | grep -i "KeyboardOptions"`
- [ ] T021 [US3] Functional testing: Navigate to Settings, enter text in password fields, verify autocorrect does not appear
- [ ] T022 [US3] Commit changes: `git add -A && git commit -m "fix(settings): Update KeyboardOptions to use autoCorrectEnabled parameter (US3)"`

**Checkpoint**: 4 deprecation warnings eliminated; password field autocorrect behavior unchanged

---

## Phase 5: User Story 4 - Google Drive AndroidHttp Migration (Priority: P4)

**Goal**: Replace deprecated AndroidHttp transport with NetHttpTransport in GoogleDriveBackup module (2 occurrences: 1 import + 1 usage)

**Independent Test**: Run `./gradlew compileDebugKotlin --warning-mode all` and verify zero deprecation warnings for AndroidHttp; perform complete Google Drive backup/restore cycle to verify functionality

### Implementation for User Story 4

- [ ] T023 [US4] Replace AndroidHttp import with NetHttpTransport in app/src/main/java/com/tiarkaerell/ibstracker/data/sync/GoogleDriveBackup.kt (line 4: remove `com.google.api.client.extensions.android.http.AndroidHttp`, add `com.google.api.client.http.javanet.NetHttpTransport`)
- [ ] T024 [US4] Replace AndroidHttp.newCompatibleTransport() with NetHttpTransport() in app/src/main/java/com/tiarkaerell/ibstracker/data/sync/GoogleDriveBackup.kt (line 297 in getDriveService() method)
- [ ] T025 [US4] Compile and verify no AndroidHttp deprecation warnings: `./gradlew compileDebugKotlin --warning-mode all 2>&1 | grep -i "AndroidHttp"`
- [ ] T026 [US4] Google Drive sync testing: Navigate to Settings → Backup/Sync, sign in with Google account, perform backup operation, verify backup file in Google Drive
- [ ] T027 [US4] Google Drive restore testing: Perform restore operation, verify data integrity, check logs for HTTP transport errors
- [ ] T028 [US4] Error scenario testing: Test with no network connection, test with invalid credentials, verify error handling works
- [ ] T029 [US4] Commit changes: `git add -A && git commit -m "fix(sync): Replace deprecated AndroidHttp with NetHttpTransport (US4)"`

**Checkpoint**: 2 deprecation warnings eliminated; Google Drive backup/restore functionality verified

---

## Phase 6: User Story 5 - CommonFoods DAO Migration (Priority: P5)

**Goal**: Remove deprecated CommonFoods.getCommonFoods() helper and migrate search functionality to DAO-based database access (affects CommonFoods.kt, FoodScreen.kt, FoodViewModel.kt)

**Independent Test**: Run `./gradlew compileDebugKotlin --warning-mode all` and verify zero deprecation warnings for CommonFoods helper; test food search functionality returns database results

### Implementation for User Story 5

- [ ] T030 [US5] Mark getCommonFoods() and searchFoods() as deprecated in app/src/main/java/com/tiarkaerell/ibstracker/data/model/CommonFoods.kt with ReplaceWith annotations pointing to DAO methods
- [ ] T031 [US5] Add searchCommonFoods(query: String): Flow<List<CommonFood>> method to app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/FoodViewModel.kt (delegates to dataRepository.searchCommonFoods())
- [ ] T032 [US5] Update FoodScreen.kt search implementation to use viewModel.searchCommonFoods() with collectAsState instead of CommonFoods.searchFoods() in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/FoodScreen.kt (around line 55)
- [ ] T033 [US5] Compile and verify no CommonFoods deprecation warnings: `./gradlew compileDebugKotlin --warning-mode all 2>&1 | grep -i "CommonFoods"`
- [ ] T034 [US5] Search functionality testing: Navigate to Food Screen, enter search queries (e.g., "yogurt"), verify search results return from database, test fuzzy matching, verify usage count sorting
- [ ] T035 [US5] Category filtering testing: Filter by different categories, verify only category foods appear, verify sorting within categories
- [ ] T036 [US5] Commit changes: `git add -A && git commit -m "fix(data): Replace deprecated CommonFoods helper with DAO-based search (US5)"`

**Checkpoint**: 1+ deprecation warnings eliminated; food search functionality uses database with reactive updates

---

## Phase 7: Final Validation & Completion

**Purpose**: Comprehensive validation of all migrations and success criteria verification

- [ ] T037 Final build with zero deprecation warnings: `./gradlew clean build --warning-mode all 2>&1 | tee build-final.log`
- [ ] T038 Verify zero deprecation warnings: `grep -i "deprecat" build-final.log | wc -l` (expected: 0)
- [ ] T039 Count replaced occurrences: `git diff main...002-fix-deprecation-warnings --stat` (should show 8 files changed)
- [ ] T040 Build time comparison: `time ./gradlew clean build` (must be within 5% of baseline from T004)
- [ ] T041 Run full test suite: `./gradlew test connectedAndroidTest` (all tests must pass)
- [ ] T042 Visual regression testing: Launch app on emulator/device, navigate all affected screens (Dashboard, Food, Analytics, Settings), verify no visual differences
- [ ] T043 Functional testing checklist: Food categories display correctly, dropdown menus work, dividers render correctly, password fields disable autocorrect, Google Drive sync works, food search returns results
- [ ] T044 Update quickstart.md with any lessons learned or migration gotchas
- [ ] T045 Final commit: `git add -A && git commit -m "docs: Update quickstart.md with migration lessons learned"`
- [ ] T046 Push branch: `git push origin 002-fix-deprecation-warnings`
- [ ] T047 Create pull request to main with summary of all migrations and success criteria met

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **User Stories (Phase 2-6)**: Each depends on Setup completion, but user stories are independent of each other
  - User stories CAN proceed in parallel (if multiple developers)
  - OR sequentially in priority order (P1 → P2 → P3 → P4 → P5) for single developer
- **Final Validation (Phase 7)**: Depends on all user stories (US1-US5) being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Setup (Phase 1) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Setup (Phase 1) - No dependencies on other stories
- **User Story 3 (P3)**: Can start after Setup (Phase 1) - No dependencies on other stories
- **User Story 4 (P4)**: Can start after Setup (Phase 1) - No dependencies on other stories
- **User Story 5 (P5)**: Can start after Setup (Phase 1) - No dependencies on other stories

**Key Insight**: All user stories are completely independent - they touch different files and can be implemented/tested in any order or in parallel.

### Within Each User Story

- Tasks marked [P] within a story can run in parallel (different files)
- Tasks without [P] must run sequentially (same file or dependencies)
- Compilation verification task runs after all file edits in story
- Testing tasks run after compilation passes
- Commit task runs after all testing passes

### Parallel Opportunities

**Between User Stories** (maximum parallelization):
- All 5 user stories (US1-US5) can be worked on simultaneously by different developers
- Each story touches different files with no conflicts

**Within User Story 1**:
- T005 (AnalyticsScreen.kt), T006 (DashboardScreen.kt), T007 (FoodScreen.kt) can run in parallel

**Within User Story 2**:
- T012 (DashboardScreen.kt dividers) and T013 (SettingsScreen.kt dividers) can run in parallel
- T014 (DashboardScreen.kt menuAnchor) and T015 (SettingsScreen.kt menuAnchor) can run in parallel
- Parts A and B can run in parallel

**Within User Story 5**:
- T030 (CommonFoods.kt), T031 (FoodViewModel.kt), T032 (FoodScreen.kt) touch different files but have logical dependencies (add ViewModel method before using it in Screen)

---

## Parallel Example: Maximum Parallelization

If you have 5 developers available after Setup (Phase 1):

```bash
# Developer A: User Story 1 (FoodCategory migrations)
Task T005: AnalyticsScreen.kt
Task T006: DashboardScreen.kt
Task T007: FoodScreen.kt
# Then T008-T011 sequentially

# Developer B: User Story 2 (Compose UI migrations)
Task T012: DashboardScreen.kt dividers
Task T013: SettingsScreen.kt dividers
Task T014: DashboardScreen.kt menuAnchor
Task T015: SettingsScreen.kt menuAnchor
# Then T016-T018 sequentially

# Developer C: User Story 3 (KeyboardOptions)
Task T019: SettingsScreen.kt (4 occurrences)
# Then T020-T022 sequentially

# Developer D: User Story 4 (AndroidHttp)
Task T023: GoogleDriveBackup.kt import
Task T024: GoogleDriveBackup.kt usage
# Then T025-T029 sequentially

# Developer E: User Story 5 (CommonFoods DAO)
Task T030: CommonFoods.kt
Task T031: FoodViewModel.kt
Task T032: FoodScreen.kt
# Then T033-T036 sequentially

# All developers reconvene for Final Validation (Phase 7)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T004)
2. Complete Phase 2: User Story 1 (T005-T011)
3. **STOP and VALIDATE**: Verify 15 deprecation warnings eliminated
4. Optional: Deploy/demo if this represents sufficient value

### Incremental Delivery (Recommended)

1. Complete Setup (Phase 1) → Baseline established
2. Add User Story 1 (P1 - 15 occurrences) → Test independently → 50% reduction in warnings
3. Add User Story 2 (P2 - 9 occurrences) → Test independently → 80% reduction in warnings
4. Add User Story 3 (P3 - 4 occurrences) → Test independently → 93% reduction in warnings
5. Add User Story 4 (P4 - 2 occurrences) → Test independently → 97% reduction in warnings
6. Add User Story 5 (P5 - 1 occurrence) → Test independently → 100% clean build
7. Final Validation (Phase 7) → Verify all success criteria met

Each story adds value without breaking previous stories; can stop at any checkpoint.

### Single Developer Strategy (Sequential)

1. Complete Setup (Phase 1)
2. Work through stories in priority order: P1 → P2 → P3 → P4 → P5
3. Commit after each story completion
4. Run compilation verification after each story
5. Final validation after all stories complete

**Estimated Time**: 2-3 hours for single developer (sequential), 1-1.5 hours with 5 developers (parallel)

---

## Success Criteria Checklist

Mark each as complete when validated:

- [ ] **SC-001**: `./gradlew clean build --warning-mode all` produces zero deprecation warnings (verified in T037-T038)
- [ ] **SC-002**: All 15+ FoodCategory.getDisplayName occurrences replaced (verified in T005-T007)
- [ ] **SC-003**: All 5 Divider components replaced with HorizontalDivider (verified in T012-T013)
- [ ] **SC-004**: All 4 menuAnchor() updated with current signature (verified in T014-T015)
- [ ] **SC-005**: All 4 KeyboardOptions constructor updated (verified in T019)
- [ ] **SC-006**: All 2 AndroidHttp replaced with NetHttpTransport (verified in T023-T024)
- [ ] **SC-007**: All 1+ CommonFoods.getCommonFoods replaced with DAO access (verified in T030-T032)
- [ ] **SC-008**: Build time within 5% of baseline (verified in T040)
- [ ] **SC-009**: All existing automated tests pass (verified in T041)
- [ ] **SC-010**: Visual regression testing shows no UI differences (verified in T042)

---

## Task Summary

**Total Tasks**: 47 tasks across 7 phases

### Task Count by Phase:
- Phase 1 (Setup): 4 tasks
- Phase 2 (US1 - FoodCategory): 7 tasks
- Phase 3 (US2 - Compose UI): 7 tasks
- Phase 4 (US3 - KeyboardOptions): 4 tasks
- Phase 5 (US4 - AndroidHttp): 7 tasks
- Phase 6 (US5 - CommonFoods): 7 tasks
- Phase 7 (Final Validation): 11 tasks

### Task Count by User Story:
- US1 (P1): 7 tasks - FoodCategory migration (highest impact)
- US2 (P2): 7 tasks - Compose UI components
- US3 (P3): 4 tasks - KeyboardOptions constructor
- US4 (P4): 7 tasks - Google Drive transport
- US5 (P5): 7 tasks - CommonFoods DAO migration

### Parallelizable Tasks:
- **14 tasks** marked [P] can run in parallel within their respective phases
- **All 5 user stories** can run in parallel (independent file changes)

### Independent Test Criteria per Story:
- **US1**: Zero FoodCategory deprecation warnings + categories display correctly
- **US2**: Zero Compose UI deprecation warnings + dividers/menus render correctly
- **US3**: Zero KeyboardOptions warnings + password autocorrect disabled
- **US4**: Zero AndroidHttp warnings + Google Drive sync functional
- **US5**: Zero CommonFoods warnings + food search returns database results

### Suggested MVP Scope:
**User Story 1 only** (P1 - FoodCategory migration) eliminates 15 of 31 warnings (~48% reduction) and affects the most critical user-facing screens. This provides immediate value with lowest risk.

---

## Format Validation

✅ **All tasks follow required checklist format**:
- All tasks start with `- [ ]` (markdown checkbox)
- All tasks have sequential Task ID (T001-T047)
- Parallelizable tasks marked with `[P]`
- User story tasks labeled with `[US1]` through `[US5]`
- All tasks include clear description with file paths
- Setup and Final Validation phases have no story labels (as required)

---

## Notes

- **[P] tasks**: Different files, no dependencies - can run in parallel
- **[Story] label**: Maps task to specific user story for traceability
- Each user story is independently completable and testable
- Commit after each story completion for clean rollback points
- Stop at any checkpoint to validate story independently
- All stories touch different files - zero merge conflicts if parallelized
- Risk level increases from P1 (lowest) to P5 (highest complexity)
