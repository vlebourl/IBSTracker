# Implementation Plan: Smart Food Categorization System

**Branch**: `001-smart-food-categorization` | **Date**: 2025-10-21 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-smart-food-categorization/spec.md`

## Summary

Reorganize food categorization from IBS-trigger-based (9 categories mixing symptoms with food types) to actual food type categories (12 categories: GRAINS, PROTEINS, DAIRY, etc.). IBS impact attributes (11 total including FODMAP levels) become hidden analytical attributes rather than primary categories. Implement guided user categorization for new foods, pre-populate ~150 common foods, and auto-update quick-add shortcuts based on usage patterns (sorted by usage count DESC, then alphabetically ASC). Includes database migration v8→v9 with automatic rollback on failure, JSON export/import for manual recovery, and EncryptedSharedPreferences for health data protection.

**Technical Approach**: Room database migration with category remapping and IBS impact assignment, Jetpack Compose UI with Material Design 3 compliance, bottom sheet dialogs for food addition/editing, StateFlow-based reactive state management, and manual dependency injection via AppContainer.

## Technical Context

**Language/Version**: Kotlin 1.8.20+ (Android)
**Primary Dependencies**:
- Jetpack Compose BOM 2023.08.00+ (Material 3, Navigation, Icons Extended)
- Room 2.6.1+ (database with KSP 1.8.20-1.0.11+)
- AndroidX Core KTX 1.10.1+, Lifecycle, ViewModel
- Kotlin Coroutines for async database operations
- EncryptedSharedPreferences (androidx.security) for data protection

**Storage**: Room (SQLite) local database with 3 tables: `food_items`, `common_foods`, `food_usage_stats`. Migration v8→v9 adds new columns and tables. EncryptedSharedPreferences for sensitive cached data.

**Testing**:
- JUnit 4.13.2 for unit tests (ViewModels, migration logic)
- AndroidX Test (JUnit 1.1.5, Espresso 3.5.1) for instrumented tests (database operations, UI)
- Manual testing on API 24+ devices (small phones to tablets)

**Target Platform**: Android 7.0+ (API 24+), target SDK 34 (Android 14)

**Project Type**: Mobile (Android single-module app)

**Performance Goals**:
- Food search: <1s p95 latency for 500+ foods database
- Quick-add update: <200ms p95 from database write to UI re-render
- Migration: <30s for 5000 historical entries with progress indicator
- App memory: <200MB during typical usage (500+ foods)

**Constraints**:
- Offline-first: All data local, no cloud sync (future enhancement)
- Clean architecture: Data/Domain/UI layer separation (NON-NEGOTIABLE)
- Material Design 3: All UI components follow MD3 guidelines
- WCAG AA: 4.5:1 contrast, 48dp touch targets minimum
- No external analytics/logging (logcat only)
- Database migration MUST preserve 100% user data with rollback safety

**Scale/Scope**:
- 12 food categories, 11 IBS impact attributes
- ~150 pre-populated common foods (bilingual EN/FR)
- User database scales to 1000+ custom foods
- 5 user stories, 53 functional requirements, 15 success criteria
- ~3 weeks estimated development (6 phases)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ I. Clean Architecture (NON-NEGOTIABLE)
- **Status**: PASS
- **Evidence**: Spec defines three layers:
  - Data: Room entities (FoodItem, CommonFood, FoodUsageStats), DAOs, DataRepository
  - Domain: FoodViewModel with StateFlow, business logic for usage tracking
  - UI: Jetpack Compose screens (FoodScreen, CategoryDetailScreen), bottom sheet dialogs
- **Verification**: No UI dependencies in data layer, no platform code in ViewModels

### ✅ II. Material Design 3 Compliance
- **Status**: PASS
- **Evidence**: FR-042 to FR-047 specify:
  - Material 3 color palette for 12 categories (semantic colors with rationale)
  - 48dp × 48dp minimum touch targets
  - WCAG AA 4.5:1 contrast ratios
  - Bottom sheet presentation for dialogs
  - Screen reader support with contentDescription
  - Ripple effects and smooth animations
- **Verification**: UI_UX_DESIGN.md provides complete Material Design 3 specifications

### ✅ III. Database Integrity & Migration Safety
- **Status**: PASS
- **Evidence**: FR-006, FR-049 specify:
  - Database version increment v8→v9
  - Explicit migration strategy with category mapping
  - Automatic rollback on failure (preserves v8 data)
  - Retry mechanism (max 3 attempts)
  - Manual JSON export/import fallback
  - Pre-population of 150 common foods
  - Migration tested with 10/100/1000/5000 entry databases
- **Verification**: DATABASE_SCHEMA.md documents complete migration strategy with SQL

### ✅ IV. Feature Documentation Before Implementation
- **Status**: PASS
- **Evidence**: Feature documentation complete before planning:
  - FEATURE_SMART_FOOD_CATEGORIZATION.md (overview, objectives, user flows)
  - DATABASE_SCHEMA.md (complete schema, migration, DAOs)
  - UI_UX_DESIGN.md (Material Design 3 specs, screen layouts)
  - IBS_ATTRIBUTES.md (11 attributes with medical rationale)
  - COMMON_FOODS.md (~150 pre-populated foods)
  - PROGRESS.md (6-phase tracker with timeline)
- **Verification**: All docs created on 2025-10-21 before implementation

### ✅ V. Versioning Discipline
- **Status**: PASS
- **Evidence**: Target version 1.9.0 (MINOR increment for new feature)
  - Current: v1.8.6 (versionCode 10)
  - Target: v1.9.0 (versionCode 11+)
  - Breaking change: Database schema v8→v9 (handled by migration)
  - Git tag: v1.9.0 on release
- **Verification**: Semantic versioning followed, versionCode increments

### ✅ VI. User-Centric Design
- **Status**: PASS
- **Evidence**: FR-037 to FR-041 specify:
  - Progressive disclosure: tooltips, educational content
  - Smart defaults: LOW_FODMAP default, pre-populated foods
  - Educational guidance: info icons with medical rationale
  - Forgiving UX: skip attributes, edit anytime, no strict validation
  - First-time tutorial and progressive tips
- **Verification**: User Story 5 dedicated to progressive education

### ✅ VII. Testing & Quality Gates
- **Status**: PASS
- **Evidence**: 15 success criteria with verification methods:
  - SC-003: Migration tests (10/100/1000/5000 entries, 0% data loss)
  - SC-007: WCAG AA automated scanner + TalkBack testing
  - SC-008: Unit tests for DAO sorting, UI integration tests
  - Unit tests for ViewModels, instrumented tests for database
  - No @Suppress or //nolint allowed
- **Verification**: Testing requirements specified in spec, constitution compliant

### Summary: All Gates Pass ✅

No constitution violations. Feature design follows all 7 core principles. Proceeding to Phase 0 research.

## Project Structure

### Documentation (this feature)

```
specs/001-smart-food-categorization/
├── spec.md              # Feature specification (COMPLETE)
├── plan.md              # This file (IN PROGRESS)
├── research.md          # Phase 0 output (PENDING)
├── data-model.md        # Phase 1 output (PENDING)
├── quickstart.md        # Phase 1 output (PENDING)
├── contracts/           # Phase 1 output (PENDING)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

**Selected Structure**: Android single-module mobile app (Option 3 variant)

```
app/src/main/java/com/tiarkaerell/ibstracker/
├── data/
│   ├── model/
│   │   ├── FoodCategory.kt          # NEW: 12-category enum with colors/icons
│   │   ├── IBSImpact.kt             # NEW: 11 IBS attribute enum
│   │   ├── AttributeCategory.kt     # NEW: Attribute grouping enum
│   │   ├── FoodItem.kt              # MODIFIED: Add category, ibsImpacts, isCustom, commonFoodId
│   │   ├── CommonFood.kt            # NEW: Pre-populated foods table
│   │   └── FoodUsageStats.kt        # NEW: Usage tracking table
│   ├── database/
│   │   ├── AppDatabase.kt           # MODIFIED: Version 8→9, add new tables
│   │   ├── Converters.kt            # MODIFIED: Add IBSImpact/FoodCategory/String list converters
│   │   ├── Migration_8_9.kt         # NEW: Database migration logic
│   │   └── dao/
│   │       ├── FoodItemDao.kt       # MODIFIED: Update queries for new schema
│   │       ├── CommonFoodDao.kt     # NEW: CRUD + search + usage tracking
│   │       └── FoodUsageStatsDao.kt # NEW: Usage analytics
│   ├── repository/
│   │   └── DataRepository.kt        # MODIFIED: Add CommonFood/FoodUsageStats methods
│   └── sync/
│       └── JsonExportImport.kt      # NEW: Export/import for migration failures
├── ui/
│   ├── screens/
│   │   ├── FoodScreen.kt            # MODIFIED: 12-category grid + quick-add
│   │   ├── CategoryDetailScreen.kt  # NEW: Foods by category with search
│   │   └── dialogs/
│   │       ├── AddFoodDialog.kt     # NEW: Guided categorization bottom sheet
│   │       └── EditFoodDialog.kt    # NEW: Edit attributes bottom sheet
│   ├── viewmodel/
│   │   └── FoodViewModel.kt         # MODIFIED: CommonFood state, usage tracking
│   ├── theme/
│   │   ├── Color.kt                 # MODIFIED: Add 12 category colors (MD3)
│   │   └── Icons.kt                 # NEW: Category icon mappings
│   └── components/
│       ├── CategoryCard.kt          # NEW: 3-column grid card
│       ├── FoodListItem.kt          # NEW: Food with usage badge
│       └── AttributeCheckbox.kt     # NEW: IBS attribute selector
└── util/
    ├── PrePopulatedFoods.kt         # NEW: 150 common foods data
    └── SessionManager.kt            # EXISTING: EncryptedSharedPreferences wrapper

app/src/test/java/
└── com/tiarkaerell/ibstracker/
    ├── viewmodel/
    │   └── FoodViewModelTest.kt     # NEW: Usage tracking, sorting tests
    └── data/
        └── migration/
            └── Migration_8_9_Test.kt # NEW: Migration logic tests

app/src/androidTest/java/
└── com/tiarkaerell/ibstracker/
    ├── database/
    │   ├── CommonFoodDaoTest.kt     # NEW: CRUD + search tests
    │   └── MigrationTest.kt         # NEW: Full migration with real DB
    └── ui/
        └── FoodScreenTest.kt        # NEW: Category grid, quick-add UI tests
```

**Structure Decision**: Android single-module architecture following existing project conventions. All new code follows clean architecture layers (data/ui/domain separation). Migration code isolated in `data/database/Migration_8_9.kt` for clear versioning. Pre-populated food data in util package for reusability across tests and production. Test structure mirrors production code for easy navigation.

## Complexity Tracking

*No violations - section not required.*

All constitution gates pass without justification needed. Feature follows established patterns:
- Manual DI via AppContainer (existing pattern)
- Room database with migrations (existing pattern)
- Jetpack Compose UI (existing pattern)
- StateFlow in ViewModels (existing pattern)
- No new frameworks or paradigms introduced

---

## Phase 1 Completion Summary

**Status**: ✅ COMPLETE (2025-10-21)

**Deliverables**:
- ✅ research.md (6 research sections with technical patterns)
- ✅ data-model.md (complete entity definitions, relationships, validation rules)
- ✅ contracts/ directory with 4 DAO interfaces:
  - FoodItemDao.kt (CRUD, search, analytics)
  - CommonFoodDao.kt (CRUD, search, usage tracking)
  - FoodUsageStatsDao.kt (usage statistics, quick-add queries)
  - DataRepository.kt (business logic, transaction coordination)
- ✅ quickstart.md (developer onboarding guide)
- ✅ CLAUDE.md updated with new technologies (Room migration, EncryptedSharedPreferences)

**Next Phase**: Phase 2 - Task Generation
- Run `/speckit.tasks` command to generate tasks.md
- Break down requirements into atomic implementation tasks
- Create prioritized development checklist
