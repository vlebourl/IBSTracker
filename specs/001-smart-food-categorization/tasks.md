# Tasks: Smart Food Categorization System

**Input**: Design documents from `/specs/001-smart-food-categorization/`
**Prerequisites**: plan.md, spec.md, data-model.md, contracts/, research.md, quickstart.md

**Tests**: Tests are included based on spec.md requirements (SC-003, SC-007, SC-008 specify explicit testing requirements)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions
- **Android mobile app**: `app/src/main/java/com/tiarkaerell/ibstracker/`
- **Tests**: `app/src/test/java/` (unit), `app/src/androidTest/java/` (instrumented)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Update gradle dependencies in app/build.gradle.kts for Room v2.6.1+, Compose BOM 2023.08.00+, EncryptedSharedPreferences
- [X] T002 [P] Create PrePopulatedFoods.kt data file in app/src/main/java/com/tiarkaerell/ibstracker/util/ with ~150 common foods from docs/food-categories/COMMON_FOODS.md
- [X] T003 [P] Update Color.kt theme file in app/src/main/java/com/tiarkaerell/ibstracker/ui/theme/ with 12 category colors (Material Design 3 extended palette)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 Create FoodCategory enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/ with 12 categories, colors, icons, bilingual names
- [X] T005 [P] Create IBSImpact enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/ with 11 attributes, grouped by AttributeCategory
- [X] T006 [P] Create AttributeCategory enum in app/src/main/java/com/tiarkaerell/ibstracker/data/model/ for UI grouping (8 values: FODMAP, GRAIN_BASED, DAIRY_BASED, STIMULANTS, IRRITANTS, MACRONUTRIENTS, CHEMICAL, ADDITIVES)
- [X] T007 Update FoodItem entity in app/src/main/java/com/tiarkaerell/ibstracker/data/model/ to add category, ibsImpacts, isCustom, commonFoodId fields
- [X] T008 [P] Create CommonFood entity in app/src/main/java/com/tiarkaerell/ibstracker/data/model/ with all fields from data-model.md
- [X] T009 [P] Create FoodUsageStats entity in app/src/main/java/com/tiarkaerell/ibstracker/data/model/ for usage tracking
- [X] T010 Update Converters.kt in app/src/main/java/com/tiarkaerell/ibstracker/data/database/ to add FoodCategory, IBSImpact List, and String List TypeConverters
- [X] T011 Create FoodItemDao in app/src/main/java/com/tiarkaerell/ibstracker/data/database/dao/ implementing all CRUD methods from contracts/FoodItemDao.kt
- [X] T012 [P] Create CommonFoodDao in app/src/main/java/com/tiarkaerell/ibstracker/data/database/dao/ implementing all methods from contracts/CommonFoodDao.kt
- [X] T013 [P] Create FoodUsageStatsDao in app/src/main/java/com/tiarkaerell/ibstracker/data/database/dao/ implementing all methods from contracts/FoodUsageStatsDao.kt
- [X] T014 Create Migration_8_9.kt in app/src/main/java/com/tiarkaerell/ibstracker/data/database/ with complete migration strategy from research.md (create tables, alter table, migrate data, pre-populate, initialize stats)
- [X] T015 Update AppDatabase.kt in app/src/main/java/com/tiarkaerell/ibstracker/data/database/ to version 9, add new entities and DAOs, register Migration_8_9
- [X] T016 Update DataRepository in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/ to add CommonFood and FoodUsageStats methods from contracts/DataRepository.kt
- [X] T017 Update AppContainer.kt in app/src/main/java/com/tiarkaerell/ibstracker/ to provide new DAOs and updated repository

**Migration Tests (SC-003 requirement)**:
- [X] T018 [P] Create Migration_8_9_Test.kt in app/src/androidTest/java/com/tiarkaerell/ibstracker/data/migration/ to test migration with 10 entries (<5s, 0% data loss)
- [X] T019 [P] Add migration test case for 100 entries (<10s, 0% data loss) in Migration_8_9_Test.kt
- [X] T020 [P] Add migration test case for 1000 entries (<20s, 0% data loss) in Migration_8_9_Test.kt
- [X] T021 [P] Add migration test case for 5000 entries (<30s, 0% data loss) in Migration_8_9_Test.kt
- [X] T022 [P] Create JsonExportImport.kt in app/src/main/java/com/tiarkaerell/ibstracker/data/sync/ for manual export/import (FR-049)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 3 - Quick Add from Usage-Sorted Shortcuts (Priority: P1) 🎯 MVP

**Goal**: Enable users to quickly log frequently-eaten foods via auto-updating shortcuts based on usage patterns

**Independent Test**: Add "Coffee" 5 times, "Bread" 3 times, "Apple" 2 times, verify quick-add section shows them in that order (Coffee, Bread, Apple) due to usage count DESC sorting

**Why MVP First**: Core UX efficiency feature - delivers immediate value to users by surfacing most-used foods. User Story 1 (add new food) depends on quick-add infrastructure, so building quick-add first establishes the foundation.

### Implementation for User Story 3

- [X] T023 [P] [US3] Create FoodUsageStatsViewModel in app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/ with StateFlow for top 6 foods
- [X] T024 [US3] Update FoodViewModel in app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/ to add usage tracking logic (calls repository.insertFoodItem which updates stats)
- [ ] T025 [US3] Create QuickAddCard composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ for individual food shortcuts with usage badge
- [ ] T026 [US3] Create QuickAddSection composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ displaying top 6 foods in grid layout
- [ ] T027 [US3] Update FoodScreen.kt in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/ to integrate QuickAddSection at top with StateFlow observation
- [ ] T028 [US3] Implement quick-add confirmation dialog in FoodScreen.kt with quantity and time fields pre-populated
- [ ] T029 [US3] Add smooth animation for quick-add re-sorting when usage counts change (SC-001: <200ms update latency)

**DAO Tests (SC-008 requirement)**:
- [ ] T030 [P] [US3] Create FoodUsageStatsDaoTest.kt in app/src/androidTest/java/com/tiarkaerell/ibstracker/database/ to test sorting (usage_count DESC, name ASC)
- [ ] T031 [P] [US3] Add test case for quick-add update latency (<200ms) in FoodUsageStatsDaoTest.kt

**Checkpoint**: At this point, quick-add shortcuts should be fully functional - users can see and tap their most-used foods

---

## Phase 4: User Story 1 - Add New Food with Guided Categorization (Priority: P1)

**Goal**: Enable users to add custom foods not in database via guided categorization dialog

**Independent Test**: Search for "homemade soup", complete guided categorization, save food, verify it appears in appropriate category sorted by usage

### Implementation for User Story 1

- [ ] T032 [P] [US1] Create CategorySelector composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ (horizontal scroll, 12 categories with colors/icons)
- [ ] T033 [P] [US1] Create FodmapLevelSelector composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ (radio buttons, default LOW_FODMAP)
- [ ] T034 [P] [US1] Create AttributeCheckbox composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ for IBS impact attributes with info icon
- [ ] T035 [P] [US1] Create AttributeSection composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ grouping attributes by AttributeCategory
- [ ] T036 [US1] Create AddFoodDialog.kt bottom sheet in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/dialogs/ integrating all selectors with conditional beverage section (FR-015)
- [ ] T037 [US1] Implement FODMAP validation in AddFoodDialog (exactly one FODMAP level required) per data-model.md
- [ ] T038 [US1] Add category-specific attribute visibility logic (show BEVERAGE attributes only when category = BEVERAGES)
- [ ] T039 [US1] Implement save logic in AddFoodDialog calling repository.insertCommonFood with isCustom = true, isVerified = false
- [ ] T040 [US1] Add search functionality to FoodScreen.kt with fuzzy matching on CommonFood.name and searchTerms (FR-033)
- [ ] T041 [US1] Implement "Add new [query]" button when search returns no results (FR-035)
- [ ] T042 [US1] Add first-time tutorial overlay to AddFoodDialog (FR-039: "Let's categorize this food!")

**UI Integration Tests**:
- [ ] T043 [P] [US1] Create AddFoodDialogTest.kt in app/src/androidTest/java/com/tiarkaerell/ibstracker/ui/ to test guided categorization flow
- [ ] T044 [P] [US1] Add test case for category-specific attribute visibility in AddFoodDialogTest.kt

**Checkpoint**: At this point, User Story 1 should be fully functional - users can add custom foods with guided categorization

---

## Phase 5: User Story 4 - Browse Foods by Category Grid (Priority: P2)

**Goal**: Enable users to explore foods organized by 12-category grid with search within category

**Independent Test**: Tap VEGETABLES category card, view all vegetables sorted by usage DESC then alphabetically, search within category "potato", navigate back to grid

### Implementation for User Story 4

- [ ] T045 [P] [US4] Create CategoryCard composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ for 12-category grid items with Material Design 3 colors/icons
- [ ] T046 [US4] Create CategoryGrid composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ with 3-column layout (responsive: 2 on small, 4 on tablets per FR-028)
- [ ] T047 [US4] Update FoodScreen.kt to integrate CategoryGrid below QuickAddSection
- [ ] T048 [P] [US4] Create CategoryDetailScreen.kt in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/ showing foods filtered by category
- [ ] T049 [US4] Implement in-category search field in CategoryDetailScreen (FR-031)
- [ ] T050 [US4] Create FoodListItem composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ displaying food with usage badge
- [ ] T051 [US4] Implement "+ Add New [Category]" button in CategoryDetailScreen with category pre-selected (FR-032)
- [ ] T052 [US4] Add Navigation route for CategoryDetailScreen in MainActivity NavHost
- [ ] T053 [US4] Implement sorting logic (usage_count DESC, name ASC) in CategoryDetailScreen StateFlow

**Checkpoint**: At this point, User Story 4 should be fully functional - users can browse foods by category grid

---

## Phase 6: User Story 2 - Edit Existing Food Attributes (Priority: P2)

**Goal**: Enable users to refine food attributes via long-press context menu

**Independent Test**: Navigate to BEVERAGES category, long-press "Coffee", select "Edit Attributes", add ACIDIC checkbox, save, verify attribute appears

### Implementation for User Story 2

- [ ] T054 [P] [US2] Create EditFoodDialog.kt bottom sheet in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/dialogs/ pre-filled with current food values
- [ ] T055 [US2] Implement long-press detection on FoodListItem composable (adds context menu overlay)
- [ ] T056 [US2] Create ContextMenu composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ with "Edit Attributes" and "Delete" options (FR-018 - "Add Favorite" is future feature)
- [ ] T057 [US2] Implement category change warning dialog in EditFoodDialog when category-specific attributes would be removed (FR-021)
- [ ] T058 [US2] Add delete prevention for verified common foods (isVerified = true) in EditFoodDialog (FR-022)
- [ ] T059 [US2] Implement save logic calling repository.updateCommonFood with FODMAP validation

**Checkpoint**: At this point, User Story 2 should be fully functional - users can edit food attributes via long-press

---

## Phase 7: User Story 5 - Progressive IBS Education Through Tooltips (Priority: P3)

**Goal**: Educate users about FODMAP and IBS attributes via tooltips and progressive tips

**Independent Test**: Trigger first-time tutorial, tap info icons (ℹ️) for FODMAP and attributes, verify simple medical-backed explanations appear

### Implementation for User Story 5

- [ ] T060 [P] [US5] Create TooltipDialog composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/ for educational content
- [ ] T061 [P] [US5] Create TooltipContent.kt data class in app/src/main/java/com/tiarkaerell/ibstracker/ui/model/ with all FODMAP and attribute explanations from docs/food-categories/IBS_ATTRIBUTES.md
- [ ] T062 [US5] Add info icons (ℹ️) to FODMAP selector in AddFoodDialog and EditFoodDialog (FR-037, FR-038)
- [ ] T063 [US5] Add info icons to all attribute sections with tooltip triggers
- [ ] T064 [US5] Implement progressive tips system showing milestone messages (after 5 foods: "long-press to edit", after 10 foods: stats summary per FR-040)
- [ ] T065 [US5] Create IBS Attributes Glossary screen in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/ with "?" help link (FR-041)
- [ ] T066 [US5] Add Navigation route for Glossary screen in MainActivity NavHost

**Checkpoint**: All user stories should now be independently functional with educational features

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

**Material Design 3 & Accessibility (FR-042 to FR-047, SC-007)**:
- [ ] T067 [P] Verify all interactive elements have minimum 48dp × 48dp touch targets across all screens
- [ ] T068 [P] Run WCAG AA contrast checker on all category colors and text (4.5:1 minimum)
- [ ] T069 [P] Add contentDescription to all Icon composables for screen reader support (FR-046)
- [ ] T070 [P] Implement ripple effects on all clickable elements per Material Design 3 (FR-047)
- [ ] T071 [P] Test with TalkBack screen reader on real device (SC-007 requirement)

**Security & Privacy (FR-050, FR-051)**:
- [ ] T072 [P] Implement EncryptedSharedPreferences wrapper in SessionManager.kt for food tracking data encryption
- [ ] T073 [P] Add EncryptedSharedPreferences fallback logic for decryption failures in SessionManager.kt

**Observability (FR-052, FR-053)**:
- [ ] T074 [P] Add logcat ERROR logging for migration failures in Migration_8_9.kt with full stack traces
- [ ] T075 [P] Add logcat ERROR logging for data corruption errors in DataRepository.kt

**Performance Optimization**:
- [ ] T076 [P] Profile search query performance in CommonFoodDao (<1s for 500+ foods per SC-001)
- [ ] T077 [P] Profile quick-add update latency (<200ms from write to UI re-render per SC-001, FR-048)
- [ ] T078 [P] Profile app memory usage during typical operation (<200MB per plan.md)

**Documentation & Validation**:
- [ ] T079 [P] Update CLAUDE.md with migration strategy and new entities
- [ ] T080 [P] Create user-facing changelog for v1.9.0 in docs/
- [ ] T081 Run quickstart.md validation checklist (manual testing on API 24+ device)
- [ ] T082 Verify all Success Criteria from spec.md (SC-001 through SC-015)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 3 (Phase 3)**: Depends on Foundational (Phase 2) - No dependencies on other stories
- **User Story 1 (Phase 4)**: Depends on US3 completion (uses quick-add infrastructure) - Can integrate with US3
- **User Story 4 (Phase 5)**: Depends on Foundational (Phase 2) - Can start after Foundational, integrates with US1
- **User Story 2 (Phase 6)**: Depends on US1 and US4 completion (edits foods added in US1, uses UI from US4) - Integrates with US1/US4
- **User Story 5 (Phase 7)**: Depends on US1 completion (adds tooltips to US1 dialogs) - Integrates with US1
- **Polish (Phase 8)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 3 (P1)**: Can start after Foundational (Phase 2) - MVP foundation
- **User Story 1 (P1)**: Depends on US3 (uses quick-add infrastructure)
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - Independent of US1/US3 but integrates with US1
- **User Story 2 (P2)**: Depends on US1 and US4 (edits foods from US1, uses category navigation from US4)
- **User Story 5 (P3)**: Depends on US1 (adds education to US1 dialogs)

### Within Each User Story

- Models before services
- Services before UI components
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel (T002, T003)
- All Foundational entity creation tasks marked [P] can run in parallel (T005, T006, T008, T009, T012, T013)
- All migration test cases marked [P] can run in parallel (T018, T019, T020, T021, T022)
- All US3 DAO tests marked [P] can run in parallel (T030, T031)
- All US1 component creation tasks marked [P] can run in parallel (T032, T033, T034, T035)
- All US1 UI tests marked [P] can run in parallel (T043, T044)
- All US4 component creation tasks marked [P] can run in parallel (T045, T048, T050)
- All US2 component creation tasks marked [P] can run in parallel (T054, T056)
- All US5 content creation tasks marked [P] can run in parallel (T060, T061, T062, T063)
- All Polish tasks marked [P] can run in parallel (T067-T080)

---

## Parallel Example: User Story 3 (Quick Add)

```bash
# Launch DAO tests together:
Task: "Create FoodUsageStatsDaoTest.kt to test sorting"
Task: "Add test case for quick-add update latency (<200ms)"

# Launch ViewModel and UI components together:
Task: "Create FoodUsageStatsViewModel with StateFlow for top 6 foods"
Task: "Create QuickAddCard composable for individual shortcuts"
Task: "Create QuickAddSection composable displaying top 6 foods"
```

---

## Implementation Strategy

### MVP First (User Story 3 + User Story 1)

1. Complete Phase 1: Setup (3 tasks, ~2 hours)
2. Complete Phase 2: Foundational (19 tasks, ~3-4 days) - CRITICAL
3. Complete Phase 3: User Story 3 (9 tasks, ~2 days) - Quick-add shortcuts MVP
4. Complete Phase 4: User Story 1 (13 tasks, ~3 days) - Add new food functionality
5. **STOP and VALIDATE**: Test US3 + US1 independently on real device
6. Deploy/demo if ready (v1.9.0-beta1)

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready (~4 days)
2. Add User Story 3 → Test independently → Deploy/Demo (Quick-add MVP! ~2 days)
3. Add User Story 1 → Test independently → Deploy/Demo (Full add food flow! ~3 days)
4. Add User Story 4 → Test independently → Deploy/Demo (Category browsing! ~2 days)
5. Add User Story 2 → Test independently → Deploy/Demo (Edit functionality! ~1-2 days)
6. Add User Story 5 → Test independently → Deploy/Demo (Education! ~1-2 days)
7. Polish → Deploy v1.9.0 final (~2-3 days)

Total: ~13-18 days (matches plan.md estimate of 3 weeks)

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (~4 days)
2. Once Foundational is done:
   - Developer A: User Story 3 (quick-add)
   - Developer B: User Story 4 (category grid) - can start in parallel
3. After US3 complete:
   - Developer A: User Story 1 (add food) - integrates with US3
   - Developer B: Continues US4
4. After US1 and US4 complete:
   - Developer A: User Story 5 (education) - integrates with US1
   - Developer B: User Story 2 (edit) - integrates with US1/US4
5. Both developers: Polish tasks in parallel

---

## Task Summary

**Total Tasks**: 82 tasks
- Phase 1 (Setup): 3 tasks
- Phase 2 (Foundational): 19 tasks (includes 5 migration tests)
- Phase 3 (US3 - Quick Add): 9 tasks (includes 2 DAO tests)
- Phase 4 (US1 - Add Food): 13 tasks (includes 2 UI tests)
- Phase 5 (US4 - Category Grid): 9 tasks
- Phase 6 (US2 - Edit Food): 6 tasks
- Phase 7 (US5 - Education): 7 tasks
- Phase 8 (Polish): 16 tasks

**Task Distribution by User Story**:
- US1 (Add New Food): 13 tasks
- US2 (Edit Food): 6 tasks
- US3 (Quick Add): 9 tasks
- US4 (Category Grid): 9 tasks
- US5 (Education): 7 tasks
- Foundation: 22 tasks (Setup + Foundational)
- Polish: 16 tasks

**Parallel Opportunities Identified**: 28 tasks marked [P] can run in parallel within their phases

**Independent Test Criteria**:
- US3: Add foods with varying usage counts, verify quick-add sorting
- US1: Search non-existent food, complete categorization, verify in category list
- US4: Navigate category grid, search within category, verify sorting
- US2: Long-press food, edit attributes, verify changes persist
- US5: Tap info icons, verify educational tooltips appear

**Suggested MVP Scope**: User Story 3 (Quick Add) + User Story 1 (Add Food) = Core food logging functionality with smart shortcuts

---

## Format Validation

✅ All 82 tasks follow the required checklist format:
- Checkbox: `- [ ]` at start
- Task ID: Sequential T001-T082
- [P] marker: 28 tasks marked as parallelizable
- [Story] label: 44 tasks labeled with US1-US5
- Description: Clear action with exact file path
- Setup/Foundational/Polish tasks: No story label (correct)

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Tests included based on SC-003, SC-007, SC-008 explicit requirements from spec.md
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies that break independence
- Follow Material Design 3 guidelines throughout (FR-042 to FR-047)
- Maintain WCAG AA accessibility compliance (FR-044)
- All database operations must preserve data integrity (FR-006)
- Quick-add updates must be fast (<200ms per FR-048)
