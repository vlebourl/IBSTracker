# Tasks: Fix Deprecation Compilation Warnings

**Input**: Design documents from `/specs/003-fix-deprecation-warnings/`
**Prerequisites**: plan.md, spec.md, research.md, quickstart.md

**Tests**: No test generation requested - feature relies on existing test suite for regression verification

**Organization**: Tasks are grouped by user story (priority P1, P2, P3) to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Android mobile app structure:
- **Main source**: `app/src/main/java/com/tiarkaerell/ibstracker/`
- **Tests**: `app/src/test/`, `app/src/androidTest/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare development environment and baseline verification

- [ ] T001 Checkout branch `003-fix-deprecation-warnings` and ensure clean working directory
- [ ] T002 Run baseline build to capture current deprecation warnings: `./gradlew clean compileDebugKotlin --warning-mode all > /tmp/baseline_warnings.txt`
- [ ] T003 [P] Run existing test suite to establish baseline: `./gradlew test` (all tests must pass)

**Checkpoint**: Development environment ready with baseline established

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: No foundational phase needed - all tasks are independent API replacements

**⚠️ CRITICAL**: This feature has no blocking prerequisites. All user story phases can begin immediately after Setup.

**Checkpoint**: Foundation ready - user story implementation can begin in parallel

---

## Phase 3: User Story 1 - Clean Build Output (Priority: P1) 🎯 MVP

**Goal**: Eliminate Quick Win deprecation warnings (8 fixes) that can be completed in 1-2 hours with zero risk. Delivers immediate value by removing most build noise.

**Independent Test**: Run `./gradlew compileDebugKotlin --warning-mode all` and verify 8 specific warnings eliminated (Material Icons 6x, Compose UI 2x)

### Implementation for User Story 1 (Quick Wins)

**Group 1A: Material Icons - AutoMirrored (6 fixes, ~5 minutes)**

- [ ] T004 [P] [US1] Replace Icons.Filled.TrendingUp with Icons.AutoMirrored.Filled.TrendingUp in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/PatternsTab.kt:77
- [ ] T005 [P] [US1] Replace Icons.Filled.TrendingUp with Icons.AutoMirrored.Filled.TrendingUp in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/PatternsTab.kt:420
- [ ] T006 [P] [US1] Replace Icons.Filled.Help with Icons.AutoMirrored.Filled.Help in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt:97
- [ ] T007 [P] [US1] Replace Icons.Filled.HelpOutline with Icons.AutoMirrored.Filled.HelpOutline in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt:958
- [ ] T008 [P] [US1] Replace Icons.Filled.ArrowBack with Icons.AutoMirrored.Filled.ArrowBack in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt:1341
- [ ] T009 [P] [US1] Replace Icons.Filled.ArrowForward with Icons.AutoMirrored.Filled.ArrowForward in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt:1360

**Group 1B: Compose UI Components (2 fixes, ~3 minutes)**

- [ ] T010 [P] [US1] Replace Indicator with SecondaryIndicator in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt:230
- [ ] T011 [P] [US1] Remove deprecated getCommonFoods() function from app/src/main/java/com/tiarkaerell/ibstracker/data/model/CommonFoods.kt:38 (verify no usages exist)

### Verification for User Story 1

- [ ] T012 [US1] Run incremental build: `./gradlew compileDebugKotlin --warning-mode all` (verify 8 warnings eliminated)
- [ ] T013 [US1] Open Analytics screen → verify icons display correctly (no visual changes)
- [ ] T014 [US1] Navigate tabs → verify tab indicator displays correctly
- [ ] T015 [US1] Run existing tests: `./gradlew test` (100% pass rate required)
- [ ] T016 [US1] Commit changes: "fix: Migrate Material Icons to AutoMirrored and update Compose UI components"

**Checkpoint**: User Story 1 complete - 8 warnings eliminated, MVP deliverable

---

## Phase 4: User Story 2 - Future-Proof Codebase (Priority: P2)

**Goal**: Fix Moderate Effort deprecations (3 fixes, 2-4 hours) that require careful testing but have well-documented migration paths. Ensures compatibility with future library updates.

**Independent Test**: Verify progress indicator animates, authentication works, ViewModels inject correctly. No runtime errors after API replacements.

### Implementation for User Story 2 (Moderate Effort)

**Group 2A: LinearProgressIndicator Lambda (1 fix, ~15 minutes)**

- [ ] T017 [P] [US2] Read AnalysisScreen.kt:1323 to identify current progress value (likely analysisProgress variable)
- [ ] T018 [US2] Replace LinearProgressIndicator(progress: Float) with LinearProgressIndicator(progress = { progressValue }) in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt:1323
- [ ] T019 [US2] Test progress indicator: Trigger analysis → verify progress bar animates smoothly

**Group 2B: Code Quality Warnings (2 fixes, ~15 minutes)**

- [ ] T020 [P] [US2] Read CredentialManagerAuth.kt:132 and simplify always-true condition in app/src/main/java/com/tiarkaerell/ibstracker/data/auth/CredentialManagerAuth.kt:132
- [ ] T021 [P] [US2] Add @Suppress("UNCHECKED_CAST") annotation with comment in app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/ViewModelFactory.kt:35 (verify isAssignableFrom check exists)

### Verification for User Story 2

- [ ] T022 [US2] Run incremental build: `./gradlew compileDebugKotlin --warning-mode all` (verify 3 additional warnings eliminated, total 11/13)
- [ ] T023 [US2] Test progress: Trigger analysis → verify progress bar displays and animates
- [ ] T024 [US2] Test authentication: Run auth tests → verify login flow works
- [ ] T025 [US2] Test ViewModels: Open all screens → verify ViewModels inject correctly
- [ ] T026 [US2] Run existing tests: `./gradlew test` (100% pass rate required)
- [ ] T027 [US2] Commit changes: "fix: Update LinearProgressIndicator and address code quality warnings"

**Checkpoint**: User Story 2 complete - 11/13 warnings eliminated, future library updates safe

---

## Phase 5: User Story 3 - Improved Code Quality Metrics (Priority: P3)

**Goal**: Fix Android Framework API deprecations (2 fixes, 4-6 hours) that require extensive cross-platform testing. Demonstrates professional code stewardship and modern Android practices.

**Independent Test**: Run static analysis tools → verify zero deprecation warnings. Test across Android 7.0-14 → verify status bar and locale changes work correctly.

### Implementation for User Story 3 (Requires Testing)

**Group 3A: Window Insets (1 fix, ~2 hours)**

- [ ] T028 [P] [US3] Read Theme.kt:96 to understand current statusBarColor usage
- [ ] T029 [US3] Replace statusBarColor with WindowCompat.setDecorFitsSystemWindows() and WindowInsetsControllerCompat in app/src/main/java/com/tiarkaerell/ibstracker/ui/theme/Theme.kt:96
- [ ] T030 [US3] Add LocalView.current and SideEffect for proper Compose integration
- [ ] T031 [US3] Configure isAppearanceLightStatusBars based on darkTheme parameter
- [ ] T032 [US3] Test status bar: Light theme → dark icons visible, Dark theme → light icons visible
- [ ] T033 [US3] Test edge-to-edge: Swipe from edges → no UI clipping
- [ ] T034 [US3] Test on Android 7.0 (API 26), Android 10 (API 29), Android 11+ (API 30+) if possible

**Group 3B: Configuration Context (1 fix, ~2 hours)**

- [ ] T035 [P] [US3] Read LocaleHelper.kt:30 to understand current updateConfiguration() usage
- [ ] T036 [US3] Replace updateConfiguration() with createConfigurationContext() in app/src/main/java/com/tiarkaerell/ibstracker/utils/LocaleHelper.kt:30
- [ ] T037 [US3] Verify MainActivity.attachBaseContext() uses returned context (not mutating newBase)
- [ ] T038 [US3] Test locale change: Settings → change language (English ↔ French) → verify UI updates
- [ ] T039 [US3] Test persistence: Restart app → verify language setting persists
- [ ] T040 [US3] Test formatting: Check date/number formatting matches selected locale

### Verification for User Story 3

- [ ] T041 [US3] Run full build: `./gradlew clean compileDebugKotlin --warning-mode all` (verify ZERO deprecation warnings)
- [ ] T042 [US3] Run full build release: `./gradlew compileReleaseKotlin --warning-mode all` (verify ZERO deprecation warnings)
- [ ] T043 [US3] Run full test suite: `./gradlew test` (100% pass rate required)
- [ ] T044 [US3] Run instrumented tests: `./gradlew connectedAndroidTest` (all pass)
- [ ] T045 [US3] Run Android Lint: `./gradlew lint` (verify improved scores)
- [ ] T046 [US3] Commit changes: "fix: Migrate deprecated Android framework APIs to modern alternatives"

**Checkpoint**: User Story 3 complete - ALL 13 warnings eliminated, production-ready code quality

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and documentation across all user stories

- [ ] T047 [P] Run final full build: `./gradlew clean build` (verify BUILD SUCCESSFUL)
- [ ] T048 [P] Verify Android Studio IDE shows zero deprecation highlighting across all Kotlin files
- [ ] T049 Manual QA: Open app → Dashboard → Food → Symptoms → Analytics → Settings (verify no crashes, UI renders correctly)
- [ ] T050 Manual QA: Test dark/light theme toggle → verify UI adapts correctly
- [ ] T051 Manual QA: Test core workflows: Add food → Add symptom → Run analysis → Change language
- [ ] T052 Compare baseline warnings (T002) vs final: Verify exactly 13 warnings eliminated
- [ ] T053 Update CLAUDE.md if needed: Document that deprecation warnings are eliminated
- [ ] T054 Run quickstart.md validation: Follow verification checklist to confirm all success criteria met
- [ ] T055 Create PR with comprehensive description: List all 13 fixes, testing performed, verification results

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: N/A - no blocking prerequisites for this feature
- **User Stories (Phase 3-5)**: All can start immediately after Setup (Phase 1)
  - User stories can proceed in parallel (if staffed) with different team members
  - Or sequentially in priority order (P1 → P2 → P3) for risk mitigation
- **Polish (Phase 6)**: Depends on all user stories being complete (US1, US2, US3)

### User Story Dependencies

- **User Story 1 (P1) - Quick Wins**: Can start immediately after Setup - 8 independent icon/component fixes
- **User Story 2 (P2) - Moderate**: Can start immediately after Setup - 3 independent fixes (no dependency on US1)
- **User Story 3 (P3) - Testing Required**: Can start immediately after Setup - 2 independent fixes (no dependency on US1/US2)

**Critical Insight**: All user stories are completely independent. They can be executed in any order or in parallel without conflicts.

### Within Each User Story

**User Story 1 (Quick Wins)**:
- T004-T009 (Material Icons) can ALL run in parallel [P] - different line numbers in different files
- T010-T011 (Compose UI) can run in parallel [P] - different files
- T012-T016 (Verification) must run sequentially after implementation

**User Story 2 (Moderate)**:
- T017-T019 (Progress Indicator) must run sequentially - same file, need to read first
- T020-T021 (Code Quality) can run in parallel [P] - different files
- T022-T027 (Verification) must run sequentially after implementation

**User Story 3 (Testing Required)**:
- T028-T034 (Window Insets) must run sequentially - extensive testing required
- T035-T040 (Configuration) must run sequentially - extensive testing required
- T028-T034 and T035-T040 can run in parallel [P] - different files
- T041-T046 (Verification) must run sequentially after implementation

### Parallel Opportunities

**Maximum Parallelism (3 developers)**:
```bash
# After T001-T003 (Setup) complete:

# Developer A: User Story 1 (Quick Wins) - 1-2 hours
Task: "T004-T011 in parallel (8 fixes)"

# Developer B: User Story 2 (Moderate) - 2-4 hours
Task: "T017-T021 (3 fixes, some parallel)"

# Developer C: User Story 3 (Testing) - 4-6 hours
Task: "T028-T040 (2 fixes, both parallel)"

# All converge for final verification (Phase 6)
```

**Within User Story 1** (8 fixes can run in parallel):
```bash
# All Material Icon fixes can launch together:
Task: "T004: Replace TrendingUp in PatternsTab.kt:77"
Task: "T005: Replace TrendingUp in PatternsTab.kt:420"
Task: "T006: Replace Help in AnalysisScreen.kt:97"
Task: "T007: Replace HelpOutline in AnalysisScreen.kt:958"
Task: "T008: Replace ArrowBack in AnalysisScreen.kt:1341"
Task: "T009: Replace ArrowForward in AnalysisScreen.kt:1360"

# Compose UI fixes can also launch in parallel:
Task: "T010: Replace Indicator in AnalysisScreen.kt:230"
Task: "T011: Remove getCommonFoods() in CommonFoods.kt:38"
```

**Within User Story 3** (2 major fix groups can run in parallel):
```bash
# Window Insets group:
Task: "T028-T034: Fix statusBarColor in Theme.kt"

# Configuration Context group:
Task: "T035-T040: Fix updateConfiguration in LocaleHelper.kt"
```

---

## Parallel Example: User Story 1 (Quick Wins)

If you have multiple developers or want to use agent parallelism:

```bash
# Launch all 6 Material Icon fixes together (different line numbers, no conflicts):
Task: "Replace Icons.Filled.TrendingUp with Icons.AutoMirrored.Filled.TrendingUp in PatternsTab.kt:77"
Task: "Replace Icons.Filled.TrendingUp with Icons.AutoMirrored.Filled.TrendingUp in PatternsTab.kt:420"
Task: "Replace Icons.Filled.Help with Icons.AutoMirrored.Filled.Help in AnalysisScreen.kt:97"
Task: "Replace Icons.Filled.HelpOutline with Icons.AutoMirrored.Filled.HelpOutline in AnalysisScreen.kt:958"
Task: "Replace Icons.Filled.ArrowBack with Icons.AutoMirrored.Filled.ArrowBack in AnalysisScreen.kt:1341"
Task: "Replace Icons.Filled.ArrowForward with Icons.AutoMirrored.Filled.ArrowForward in AnalysisScreen.kt:1360"

# Launch Compose UI fixes in parallel (different files):
Task: "Replace Indicator with SecondaryIndicator in AnalysisScreen.kt:230"
Task: "Remove deprecated getCommonFoods() function from CommonFoods.kt:38"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only) - Recommended Approach

**Timeline**: 1-2 hours for immediate impact

1. Complete Phase 1: Setup (T001-T003) - 5 minutes
2. Complete Phase 3: User Story 1 (T004-T016) - 30 minutes implementation + 30 minutes verification
3. **STOP and VALIDATE**: Run build → verify 8/13 warnings eliminated
4. Commit and push MVP → immediate value delivered
5. **Decision point**: Continue to US2/US3 or deploy MVP first?

**Value**: Eliminates majority of build noise (61% of warnings) with zero risk in minimal time.

### Incremental Delivery (All User Stories)

**Timeline**: 8-12 hours total (1-1.5 days)

1. Complete Setup (Phase 1) → 5 minutes
2. Complete User Story 1 (Phase 3) → Test independently → Commit (8/13 warnings gone) ✅ MVP!
3. Complete User Story 2 (Phase 4) → Test independently → Commit (11/13 warnings gone) ✅ Enhanced!
4. Complete User Story 3 (Phase 5) → Test independently → Commit (13/13 warnings gone) ✅ Perfect!
5. Complete Polish (Phase 6) → Final verification → Create PR ✅ Production-ready!

**Each phase adds incremental value without breaking previous work.**

### Parallel Team Strategy

With 3 developers available:

1. **Together**: Complete Setup (Phase 1) - 5 minutes
2. **Split work** (after Setup):
   - **Developer A**: User Story 1 (T004-T016) - Quick Wins, lowest risk
   - **Developer B**: User Story 2 (T017-T027) - Moderate effort
   - **Developer C**: User Story 3 (T028-T046) - Highest effort, most testing
3. **Reconverge**: All developers complete → run Phase 6 (Polish) together
4. **Review**: Cross-review each other's PRs or merge to single PR

**Timeline**: 4-6 hours wall-clock time (parallelized from 8-12 hours)

---

## Risk Mitigation by User Story

| User Story | Risk Level | Mitigation Strategy | Rollback Plan |
|-----------|------------|---------------------|---------------|
| US1 (Quick Wins) | ✅ Very Low | Simple imports/renames, extensive precedent | Revert commit, zero functional impact |
| US2 (Moderate) | ⚠️ Low | Well-documented migrations, existing tests | Revert commit, tests catch issues |
| US3 (Testing) | ⚠️ Medium | Cross-platform testing, gradual rollout | Revert commit, test on multiple devices first |

**Recommended Sequence**: US1 → US2 → US3 (increasing risk/effort, decreasing reward)

---

## Success Metrics

| Metric | Target | Verification Method | Task |
|--------|--------|---------------------|------|
| Debug Build Warnings | 0 | `./gradlew compileDebugKotlin --warning-mode all` | T041 |
| Release Build Warnings | 0 | `./gradlew compileReleaseKotlin --warning-mode all` | T042 |
| Unit Test Pass Rate | 100% | `./gradlew test` | T043 |
| Instrumented Test Pass Rate | 100% | `./gradlew connectedAndroidTest` | T044 |
| IDE Deprecation Highlighting | 0 files | Open all .kt files in Android Studio | T048 |
| Manual QA Regressions | 0 issues | Full app workflow testing | T049-T051 |
| Static Analysis Score | Improved | `./gradlew lint` | T045 |

---

## Task Summary

**Total Tasks**: 55
- **Setup**: 3 tasks
- **User Story 1 (P1)**: 13 tasks (8 implementation + 5 verification)
- **User Story 2 (P2)**: 11 tasks (5 implementation + 6 verification)
- **User Story 3 (P3)**: 19 tasks (13 implementation + 6 verification)
- **Polish**: 9 tasks (final verification + documentation)

**Parallel Opportunities**:
- **Within US1**: 8 fixes can run in parallel (T004-T011)
- **Within US2**: 2 fixes can run in parallel (T020-T021)
- **Within US3**: 2 major groups can run in parallel (T028-T034 + T035-T040)
- **Across Stories**: All 3 user stories can run in parallel (if staffed)

**Independent Test Criteria**:
- **US1**: `./gradlew compileDebugKotlin` shows 8 warnings eliminated
- **US2**: Progress bar animates, auth/ViewModels work, 11 warnings eliminated
- **US3**: Status bar + locale work across Android versions, 13 warnings eliminated

**Suggested MVP Scope**: User Story 1 only (T001-T016) - delivers 61% value in 20% time

---

## Notes

- [P] tasks = different files or line numbers, no dependencies
- [Story] label (US1, US2, US3) maps task to specific user story for traceability
- Each user story is independently completable and testable
- No test generation requested - relying on existing comprehensive test suite
- Commit after each user story completion for easy rollback
- Stop at any checkpoint to validate story independently before proceeding
- All file paths are absolute from repository root for clarity
- **Format compliance**: All tasks follow `- [ ] [TaskID] [P?] [Story?] Description with file path` format
