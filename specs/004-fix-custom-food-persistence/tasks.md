# Tasks: Fix Custom Food Addition Bug

**Input**: Design documents from `/specs/004-fix-custom-food-persistence/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Included per quickstart.md TDD approach

**Organization**: Tasks grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Android single-module app structure:
- **Main code**: `app/src/main/java/com/tiarkaerell/ibstracker/`
- **Unit tests**: `app/src/test/java/com/tiarkaerell/ibstracker/`
- **Instrumented tests**: `app/src/androidTest/java/com/tiarkaerell/ibstracker/`

---

## Phase 1: Setup (Preparation)

**Purpose**: Verify existing infrastructure and understand current implementation

- [X] T001 Read and understand existing schema in app/src/main/java/com/tiarkaerell/ibstracker/data/database/AppDatabase.kt
- [X] T002 [P] Read CommonFood entity structure in app/src/main/java/com/tiarkaerell/ibstracker/data/model/CommonFood.kt
- [X] T003 [P] Read FoodItem entity structure in app/src/main/java/com/tiarkaerell/ibstracker/data/model/FoodItem.kt
- [X] T004 [P] Review CommonFoodDao queries in app/src/main/java/com/tiarkaerell/ibstracker/data/database/dao/CommonFoodDao.kt
- [X] T005 [P] Review FoodItemDao queries in app/src/main/java/com/tiarkaerell/ibstracker/data/database/dao/FoodItemDao.kt
- [X] T006 Read current insertFoodItem() implementation in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/DataRepository.kt
- [X] T007 [P] Read FoodViewModel.saveFoodItem() flow in app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/FoodViewModel.kt
- [X] T008 [P] Verify UI displays from CommonFood in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/FoodScreen.kt
- [X] T009 Review Migration_8_9 pattern for CommonFood creation in app/src/main/java/com/tiarkaerell/ibstracker/data/database/Migration_8_9.kt

**Checkpoint**: Codebase understanding complete - ready to write tests

---

## Phase 2: Foundational (No Blocking Prerequisites)

**Purpose**: This bug fix has no foundational prerequisites - all infrastructure exists

**⚠️ NOTE**: Schema v9 already supports all needed fields. No migration, no new DAOs, no new entities required.

**Checkpoint**: Foundation ready (already exists) - user story implementation can begin immediately

---

## Phase 3: User Story 1 - Add Custom Food to Category (Priority: P1) 🎯 MVP

**Goal**: Fix core bug so custom foods (e.g., "Soja") appear in category lists and search results immediately after save

**Independent Test**: Add custom food "Soja" to "Other" category → Verify appears in category list + search results

### Tests for User Story 1 (TDD Approach)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T010 [P] [US1] Create test file app/src/androidTest/java/com/tiarkaerell/ibstracker/data/repository/CustomFoodPersistenceTest.kt
- [X] T011 [P] [US1] Write test testCustomFoodCreatesCommonFood() - verify CommonFood created with is_verified=false in CustomFoodPersistenceTest.kt
- [X] T012 [P] [US1] Write test testDuplicateCustomFoodReusesCommonFood() - verify no duplicate CommonFood entries in CustomFoodPersistenceTest.kt
- [X] T013 [P] [US1] Write test testCustomFoodAppearsInCategoryList() - verify custom food visible via getCommonFoodsByCategory() in CustomFoodPersistenceTest.kt
- [X] T014 [P] [US1] Write test testCustomFoodAppearsInSearch() - verify custom food visible via searchCommonFoods() in CustomFoodPersistenceTest.kt
- [X] T015 [US1] Run tests with ./gradlew connectedAndroidTest - verify all 4 tests FAIL (bug not fixed yet)

### Implementation for User Story 1

- [X] T016 [US1] Modify DataRepository.insertFoodItem() to check for existing CommonFood via getCommonFoodByName().first() in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/DataRepository.kt
- [X] T017 [US1] Add logic to create new CommonFood if not found (is_verified=false, ibsImpacts=[FODMAP_LOW], category from foodItem) in DataRepository.kt
- [X] T018 [US1] Update FoodItem creation to include commonFoodId from CommonFood.id in DataRepository.kt
- [X] T019 [US1] Ensure CommonFood.usage_count increments for all foods (not just existing) in DataRepository.kt
- [X] T020 [US1] Update upsertUsageStats call to set isFromCommonFoods=true for custom foods in DataRepository.kt
- [X] T021 [US1] Run tests with ./gradlew connectedAndroidTest - verify all 4 tests PASS
- [ ] T022 [US1] Manual test: Add custom food "Soja" to "Other" category - verify appears in category list immediately
- [ ] T023 [US1] Manual test: Search for "Soja" - verify appears in search results
- [ ] T024 [US1] Manual test: Add "Soja" again (duplicate) - verify no duplicate created, usage count increments

**Checkpoint**: User Story 1 complete - custom foods persist and appear in UI

---

## Phase 4: User Story 2 - Category Display with Custom Foods (Priority: P2)

**Goal**: Verify custom foods are sorted correctly (usage count DESC, then alphabetically ASC) and integrate seamlessly with pre-populated foods

**Independent Test**: Add 3 custom foods + log them different amounts → Verify top 6 display follows usage-based sorting

### Tests for User Story 2

- [ ] T025 [P] [US2] Write test testCategorySortingWithCustomFoods() - verify usage-based then alphabetical sort in CustomFoodPersistenceTest.kt
- [ ] T026 [P] [US2] Write test testCategoryFillsWithPrePopulatedFoods() - verify fills to 6 foods when < 6 used in CustomFoodPersistenceTest.kt
- [ ] T027 [P] [US2] Write test testCustomFoodsIntegrateWithVerifiedFoods() - verify mixed list displays correctly in CustomFoodPersistenceTest.kt
- [ ] T028 [US2] Run tests with ./gradlew connectedAndroidTest - verify tests FAIL before verification

### Verification for User Story 2

> **NOTE**: No code changes needed - existing DAO queries already support sorting

- [ ] T029 [US2] Verify getCommonFoodsByCategory() query uses ORDER BY usage_count DESC, name ASC in CommonFoodDao.kt
- [ ] T030 [US2] Manual test: Add custom food "Zucchini" to "Vegetables", log 5 times
- [ ] T031 [US2] Manual test: Log pre-populated "Carrot" 3 times
- [ ] T032 [US2] Manual test: Verify "Zucchini" appears before "Carrot" in category list (usage-based sort)
- [ ] T033 [US2] Manual test: Add 2 more custom foods with 0 usage - verify category shows top 6 (used foods + unused pre-populated)
- [ ] T034 [US2] Run tests with ./gradlew connectedAndroidTest - verify all tests PASS

**Checkpoint**: User Story 2 complete - sorting and display logic verified

---

## Phase 5: User Story 3 - Quick-Add for Custom Foods (Priority: P3)

**Goal**: Verify frequently used custom foods appear in quick-add row (top 4 most-used foods across all categories)

**Independent Test**: Log custom food "Soja" 10 times → Verify appears in quick-add row

### Tests for User Story 3

- [ ] T035 [P] [US3] Write test testCustomFoodAppearsInQuickAdd() - verify top 4 includes custom foods in CustomFoodPersistenceTest.kt
- [ ] T036 [P] [US3] Write test testQuickAddUpdatesWithUsage() - verify quick-add updates dynamically in CustomFoodPersistenceTest.kt
- [ ] T037 [US3] Run tests with ./gradlew connectedAndroidTest - verify tests FAIL before verification

### Verification for User Story 3

> **NOTE**: No code changes needed - existing DAO queries already support top N foods

- [ ] T038 [US3] Verify getTopUsedCommonFoods(limit=4) query uses ORDER BY usage_count DESC, name ASC in CommonFoodDao.kt
- [ ] T039 [US3] Verify FoodViewModel.getTopUsedFoods(limit=4) uses correct repository method in FoodViewModel.kt
- [ ] T040 [US3] Manual test: Log custom food "Tofu" 15 times (more than any pre-populated food)
- [ ] T041 [US3] Manual test: Open Food screen - verify "Tofu" appears in quick-add row (top 4 foods)
- [ ] T042 [US3] Manual test: Log another custom food "Tempeh" 20 times - verify quick-add updates to show "Tempeh" first
- [ ] T043 [US3] Run tests with ./gradlew connectedAndroidTest - verify all tests PASS

**Checkpoint**: User Story 3 complete - quick-add integration verified

---

## Phase 6: Performance & Edge Cases

**Purpose**: Validate performance with large datasets and edge cases

- [ ] T044 [P] Write test testPerformanceWith200CustomFoods() - verify p95 < 500ms for category load in CustomFoodPersistenceTest.kt
- [ ] T045 [P] Write test testSearchPerformanceWithManyFoods() - verify search < 1s for 50 results in CustomFoodPersistenceTest.kt
- [ ] T046 [P] Write test testSpecialCharactersInFoodName() - verify UTF-8 support (e.g., "Café au lait") in CustomFoodPersistenceTest.kt
- [ ] T047 [P] Write test testCaseInsensitiveDuplicates() - verify "Soja" vs "soja" creates separate entries (case-sensitive) in CustomFoodPersistenceTest.kt
- [ ] T048 [P] Write test testVeryLargeUsageCount() - verify sorting works with usage_count > 1000 in CustomFoodPersistenceTest.kt
- [ ] T049 Run performance and edge case tests with ./gradlew connectedAndroidTest
- [ ] T050 Manual test: Create 200 custom foods in "Other" category (loop in test helper)
- [ ] T051 Manual test: Measure category load time - verify < 500ms on emulator
- [ ] T052 Manual test: Search for foods - verify < 1s response time

**Checkpoint**: Performance validated - ready for code review

---

## Phase 7: Polish & Documentation

**Purpose**: Code quality, documentation, and final validation

- [ ] T053 [P] Code review checklist: Fix compiles without errors
- [ ] T054 [P] Code review checklist: All tests pass (4 core + 3 verification + 5 edge cases = 12 tests)
- [ ] T055 [P] Code review checklist: No database migration required (schema v9 supports all fields)
- [ ] T056 [P] Code review checklist: Backward compatible (old FoodItems with null commonFoodId still work)
- [ ] T057 [P] Code review checklist: No breaking changes to existing APIs
- [ ] T058 [P] Code review checklist: Performance meets targets (< 500ms, < 1s)
- [ ] T059 [P] Code review checklist: Code follows existing patterns (repository, suspend functions, Flow)
- [ ] T060 [P] Code review checklist: FODMAP validation enforced (exactly one FODMAP level)
- [ ] T061 [P] Update TODO.md - mark "Food not appearing after adding to a category" as complete
- [ ] T062 [P] Add inline documentation to modified insertFoodItem() method
- [ ] T063 Run quickstart.md validation - follow manual test steps in quickstart.md
- [ ] T064 Build release APK with ./gradlew assembleRelease
- [ ] T065 Final smoke test: Install on device, add 5 custom foods, verify all appear correctly

**Checkpoint**: Feature complete - ready for PR/merge

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately (read-only tasks)
- **Foundational (Phase 2)**: N/A - no foundational work needed (infrastructure exists)
- **User Stories (Phase 3-5)**: All depend on Setup completion
  - User Story 1 (P1): Can start immediately after Setup - **MVP CRITICAL**
  - User Story 2 (P2): Can start after US1 complete - verification only, no code changes
  - User Story 3 (P3): Can start after US1 complete - verification only, no code changes
- **Performance (Phase 6)**: Depends on User Story 1 complete
- **Polish (Phase 7)**: Depends on all user stories complete

### User Story Dependencies

- **User Story 1 (P1)**: No dependencies - implements core fix
- **User Story 2 (P2)**: Depends on US1 (verifies sorting with custom foods)
- **User Story 3 (P3)**: Depends on US1 (verifies quick-add with custom foods)

**Critical Path**: Setup → US1 → US2 → US3 → Performance → Polish

### Within Each User Story

**User Story 1** (only story with code changes):
1. Write tests first (T010-T015) - all can run in parallel
2. Run tests → verify FAIL
3. Implement fix (T016-T020) - sequential (same file)
4. Run tests → verify PASS
5. Manual testing (T021-T024)

**User Stories 2 & 3** (verification only):
1. Write tests (parallel)
2. Run tests → verify FAIL
3. Verify existing queries (no changes needed)
4. Manual testing
5. Run tests → verify PASS

### Parallel Opportunities

- **Phase 1 (Setup)**: All 9 read tasks can run in parallel (T001-T009)
- **User Story 1 Tests**: All 5 test creation tasks can run in parallel (T010-T014)
- **User Story 2 Tests**: All 3 test creation tasks can run in parallel (T025-T027)
- **User Story 3 Tests**: Both test creation tasks can run in parallel (T035-T036)
- **Performance Tests**: All 5 edge case tests can run in parallel (T044-T048)
- **Code Review**: All checklist items can be verified in parallel (T053-T060)
- **User Stories 2 & 3 can start in parallel after US1 completes** (both are verification-only)

---

## Parallel Example: User Story 1 Tests

```bash
# Launch all test creation for User Story 1 together:
Task: "Create test file CustomFoodPersistenceTest.kt"
Task: "Write test testCustomFoodCreatesCommonFood()"
Task: "Write test testDuplicateCustomFoodReusesCommonFood()"
Task: "Write test testCustomFoodAppearsInCategoryList()"
Task: "Write test testCustomFoodAppearsInSearch()"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only - RECOMMENDED)

**Timeline**: 2.5-3 hours

1. Complete Phase 1: Setup (30 min - understand codebase)
2. Complete Phase 3: User Story 1 (2 hours)
   - Write tests (15 min)
   - Implement fix (30 min)
   - Run tests (10 min)
   - Manual testing (15 min)
3. **STOP and VALIDATE**: Test User Story 1 independently
4. Deploy/demo if ready (bug fix complete!)

**Deliverable**: Custom foods persist and appear in category lists + search

---

### Incremental Delivery (All User Stories)

**Timeline**: 4-5 hours total

1. Complete Setup (30 min)
2. Add User Story 1 (2 hours) → Test independently → **Deploy/Demo MVP!**
3. Add User Story 2 (30 min) → Test independently → Verify sorting
4. Add User Story 3 (30 min) → Test independently → Verify quick-add
5. Add Performance testing (30 min) → Validate edge cases
6. Add Polish (30 min) → Code review + documentation

**Deliverable**: Complete bug fix with verification + documentation

---

### Parallel Team Strategy

With 2 developers:

1. **Developer A**: Setup (Phase 1) → User Story 1 (Phase 3)
2. **Developer B**: Can start after US1 completes:
   - User Story 2 (Phase 4) in parallel with
   - User Story 3 (Phase 5)
3. Both collaborate on Performance (Phase 6) and Polish (Phase 7)

**Timeline**: ~3 hours with parallelization

---

## Notes

- [P] tasks = different files or read-only, no dependencies
- [US1/US2/US3] labels map task to specific user story for traceability
- **Only User Story 1 requires code changes** - US2 and US3 are verification-only
- All tests follow TDD: Write test → Verify FAIL → Implement → Verify PASS
- Single file modification: `DataRepository.kt` (~15 lines of code)
- No database migration required (schema v9 already supports feature)
- Backward compatible: old FoodItems with null commonFoodId continue working
- Stop at any checkpoint to validate story independently
- Commit after each logical group of tasks

---

## Task Summary

**Total Tasks**: 65 tasks
- Phase 1 (Setup): 9 tasks (all parallel read operations)
- Phase 2 (Foundational): 0 tasks (N/A)
- Phase 3 (User Story 1): 15 tasks (5 tests + 9 implementation + 1 manual)
- Phase 4 (User Story 2): 10 tasks (3 tests + 7 verification)
- Phase 5 (User Story 3): 9 tasks (2 tests + 7 verification)
- Phase 6 (Performance): 9 tasks (5 tests + 4 validation)
- Phase 7 (Polish): 13 tasks (8 review + 5 documentation)

**Tests**: 12 automated tests + manual testing
**Code Changes**: 1 file modified (`DataRepository.kt`)
**Parallel Opportunities**: 25 parallelizable tasks marked with [P]

**MVP Scope** (User Story 1 only): 24 tasks, 2.5-3 hours
**Full Feature**: 65 tasks, 4-5 hours

**Independent Test Criteria**:
- ✅ User Story 1: Add "Soja" → appears in category list + search
- ✅ User Story 2: Log multiple custom foods → sorted by usage, then alphabetically
- ✅ User Story 3: Log "Tofu" 10+ times → appears in quick-add row
