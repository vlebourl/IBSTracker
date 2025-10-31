# Implementation Plan: Fix Custom Food Addition Bug

**Branch**: `004-fix-custom-food-persistence` | **Date**: 2025-10-27 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-fix-custom-food-persistence/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Fix critical data persistence bug where custom foods added by users are not appearing in category lists or search results. The issue stems from custom foods being saved as `FoodItem` entries without creating corresponding `CommonFood` entries. The UI displays foods from the `common_foods` table, so newly added custom foods are invisible to users.

**Technical Approach**: Modify `DataRepository.insertFoodItem()` to check for existing `CommonFood` by name, create new `CommonFood` entry if not found (with `is_verified = 0`), and link the `FoodItem` to the `CommonFood` via `commonFoodId`. This ensures all custom foods are searchable and appear in category lists with proper usage tracking.

## Technical Context

**Language/Version**: Kotlin 1.8.20 / Android SDK 34
**Primary Dependencies**:
- Jetpack Compose (Material3)
- Room Database 2.6.1 (local SQLite persistence)
- Kotlin Coroutines & Flow for reactive data
- AndroidX Core KTX 1.10.1

**Storage**: Room SQLite database (`ibs-tracker-database`, schema v9)
- Tables: `common_foods`, `food_items`, `food_usage_stats`, `symptoms`
- Current migration version: 8→9 (from smart food categorization feature)

**Testing**:
- JUnit 4.13.2 for unit tests
- AndroidX Test (JUnit 1.1.5, Espresso 3.5.1) for instrumented tests
- Room schema exports for migration validation

**Target Platform**: Android 7.0+ (API 24+), targeting Android 14 (API 34)

**Project Type**: Mobile (Android single-module app)

**Performance Goals**:
- p95 < 500ms for category load with 200+ custom foods
- < 1 second for search results (50 items)
- < 1 second for custom food to appear in UI after save

**Constraints**:
- Must maintain backward compatibility with existing `FoodItem` entries (nullable `commonFoodId`)
- No database migration required (schema v9 already supports all needed fields)
- Must not break existing pre-populated foods (72 verified CommonFood entries)
- Offline-capable (all operations local, no network dependency)

**Scale/Scope**:
- Support 200+ custom foods per category
- Handle 5000+ total FoodItem entries without degradation
- 12 food categories total
- Existing 72 pre-populated CommonFood entries

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Status**: ✅ PASS - No constitution file defined for this project

No project-specific constitution exists at `.specify/memory/constitution.md`. This is a bug fix in an existing Android application following established patterns:
- Uses existing Room database schema (v9)
- Follows existing repository pattern (`DataRepository`)
- Maintains existing DAO contracts (`CommonFoodDao`, `FoodItemDao`)
- No new architectural patterns introduced
- No breaking changes to existing APIs

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
│   ├── database/
│   │   ├── dao/
│   │   │   ├── CommonFoodDao.kt          # MODIFY: No changes (already has all needed queries)
│   │   │   ├── FoodItemDao.kt            # MODIFY: No changes (already supports commonFoodId)
│   │   │   └── FoodUsageStatsDao.kt      # MODIFY: No changes
│   │   ├── AppDatabase.kt                # READ: Verify schema v9
│   │   └── Migration_8_9.kt              # READ: Understand CommonFood creation pattern
│   ├── model/
│   │   ├── CommonFood.kt                 # READ: Understand entity structure
│   │   ├── FoodItem.kt                   # READ: Verify commonFoodId field
│   │   ├── FoodCategory.kt               # READ: Enum values
│   │   └── IBSImpact.kt                  # READ: Default FODMAP_LOW value
│   └── repository/
│       └── DataRepository.kt             # MODIFY: Fix insertFoodItem() logic
├── ui/
│   ├── viewmodel/
│   │   └── FoodViewModel.kt              # READ: Understand saveFoodItem() flow
│   └── screens/
│       └── FoodScreen.kt                 # READ: Verify UI displays from CommonFood
└── IBSTrackerApplication.kt              # READ: AppContainer initialization

app/src/test/java/
└── [unit tests to be added]

app/src/androidTest/java/
└── [instrumented tests to be added]
```

**Structure Decision**: Android single-module app following standard Android architecture:
- Data layer: Room DAOs, entities, repository pattern
- UI layer: Jetpack Compose screens with ViewModels
- Dependency injection: Manual via AppContainer

**Files Modified**: Only `DataRepository.kt` requires changes
**Files Added**: Test files for custom food persistence verification

## Complexity Tracking

**Status**: N/A - No constitution violations

This is a minimal bug fix that adds ~15 lines of code to an existing method. No new patterns or complexity introduced.
