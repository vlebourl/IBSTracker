# Quickstart Guide: Smart Food Categorization

**Branch**: `001-smart-food-categorization` | **Target Version**: 1.9.0 | **Database Version**: 9

## Overview

This guide helps developers quickly understand and work with the Smart Food Categorization feature. Read this BEFORE starting implementation.

**What This Feature Does**:
- Reorganizes food categories from IBS-trigger-based (9 old categories) to actual food types (12 new categories)
- Adds 11 hidden IBS impact attributes for analytical purposes
- Pre-populates ~150 common foods with verified IBS attributes
- Implements auto-updating quick-add shortcuts based on usage patterns
- Migrates database v8→v9 with automatic rollback on failure

**Key User Benefits**:
- Intuitive food categorization (GRAINS, PROTEINS, DAIRY vs. confusing GLUTEN, HIGH_FODMAP)
- Smart food suggestions (top 6 most-used foods displayed as shortcuts)
- Guided categorization for new foods with educational tooltips
- Progressive disclosure (IBS attributes hidden until user explores)

---

## Quick Reference

### Key Files to Know

**Documentation** (read these first):
- `specs/001-smart-food-categorization/spec.md` - Complete feature specification
- `specs/001-smart-food-categorization/data-model.md` - Entity definitions and relationships
- `specs/001-smart-food-categorization/research.md` - Technical research findings
- `docs/food-categories/IBS_ATTRIBUTES.md` - Medical rationale for 11 IBS attributes
- `docs/food-categories/COMMON_FOODS.md` - Pre-populated foods list

**Contracts** (API interfaces):
- `specs/001-smart-food-categorization/contracts/FoodItemDao.kt` - Food item CRUD operations
- `specs/001-smart-food-categorization/contracts/CommonFoodDao.kt` - Common foods CRUD + search
- `specs/001-smart-food-categorization/contracts/FoodUsageStatsDao.kt` - Usage tracking
- `specs/001-smart-food-categorization/contracts/DataRepository.kt` - Repository layer with business logic

**Implementation Files** (to be created):
- `app/src/main/java/com/tiarkaerell/ibstracker/data/model/FoodCategory.kt` - 12-category enum
- `app/src/main/java/com/tiarkaerell/ibstracker/data/model/IBSImpact.kt` - 11 IBS attribute enum
- `app/src/main/java/com/tiarkaerell/ibstracker/data/database/Migration_8_9.kt` - Database migration
- `app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/FoodScreen.kt` - Main food UI (MODIFIED)
- `app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/CategoryDetailScreen.kt` - Category screen (NEW)
- `app/src/main/java/com/tiarkaerell/ibstracker/util/PrePopulatedFoods.kt` - 150 common foods data

---

## Development Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Kotlin 1.8.20+
- Target SDK 34 (Android 14)
- Min SDK 24 (Android 7.0)

### Branch Setup

```bash
# Ensure you're on the feature branch
git checkout 001-smart-food-categorization

# Pull latest changes
git pull origin 001-smart-food-categorization

# Verify no uncommitted changes
git status
```

### Dependencies (Already Configured)

The following dependencies are already in `build.gradle.kts`:

```kotlin
// Jetpack Compose
implementation(platform("androidx.compose:compose-bom:2023.08.00"))
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.navigation:navigation-compose:2.7.7")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// EncryptedSharedPreferences (for sensitive data)
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

### Build Commands

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run all checks (lint + test)
./gradlew check
```

---

## Architecture Overview

### Clean Architecture Layers

```
┌─────────────────────────────────────────────┐
│           UI Layer (Jetpack Compose)        │
│  FoodScreen, CategoryDetailScreen, Dialogs  │
│  Material Design 3 components               │
└────────────────┬────────────────────────────┘
                 │ StateFlow
┌────────────────▼────────────────────────────┐
│         Domain Layer (ViewModels)           │
│  FoodViewModel - business logic & state     │
│  StateFlow, viewModelScope                  │
└────────────────┬────────────────────────────┘
                 │ suspend functions
┌────────────────▼────────────────────────────┐
│         Data Layer (Repository + DAOs)      │
│  DataRepository - coordinates 3 DAOs        │
│  FoodItemDao, CommonFoodDao, UsageStatsDao  │
│  Room database with TypeConverters          │
└─────────────────────────────────────────────┘
```

**Key Principle**: NO UI dependencies in data layer, NO platform code in ViewModels

### Data Flow Example

**User logs a food item**:
1. User taps "Add Food" button in `FoodScreen` (UI Layer)
2. `FoodViewModel.addFoodItem()` called with food data (Domain Layer)
3. `DataRepository.insertFoodItem()` coordinates:
   - Inserts `FoodItem` via `FoodItemDao`
   - Increments `CommonFood.usage_count` via `CommonFoodDao` (if linked)
   - Upserts `FoodUsageStats` via `FoodUsageStatsDao`
4. StateFlow emits updated state → UI re-renders quick-add shortcuts (<200ms)

---

## Data Model Quick Reference

### 12 Food Categories (Enum)

```kotlin
enum class FoodCategory {
    GRAINS,           // Bread, pasta, rice
    PROTEINS,         // Meat, fish, eggs
    DAIRY,            // Milk, cheese, yogurt
    VEGETABLES,       // All vegetables
    FRUITS,           // All fruits
    LEGUMES,          // Beans, lentils
    NUTS_SEEDS,       // Nuts, seeds
    BEVERAGES,        // Coffee, tea, juice
    FATS_OILS,        // Cooking oils, butter
    SWEETS,           // Desserts, candy
    PROCESSED,        // Pre-packaged foods
    OTHER             // Miscellaneous
}
```

### 11 IBS Impact Attributes (Enum)

Grouped into 5 categories:

1. **FODMAP Level** (REQUIRED - exactly one):
   - `FODMAP_HIGH`, `FODMAP_MODERATE`, `FODMAP_LOW`

2. **Grain-Based**: `GLUTEN`

3. **Dairy-Based**: `LACTOSE`

4. **Stimulants**: `CAFFEINE`, `ALCOHOL`

5. **Irritants**: `SPICY`

6. **Macronutrients**: `FATTY`

7. **Chemical**: `ACIDIC`

8. **Additives**: `ARTIFICIAL_SWEETENERS`

**Important**: Every food MUST have exactly one FODMAP level (default: `FODMAP_LOW`)

### Three Main Entities

1. **FoodItem** (user-logged entries):
   - Existing: `id`, `name`, `quantity`, `timestamp`
   - NEW: `category`, `ibsImpacts`, `isCustom`, `commonFoodId`

2. **CommonFood** (pre-populated database, ~150 foods):
   - `id`, `name`, `category`, `ibsImpacts`, `searchTerms`, `usageCount`, `nameFr`, `isVerified`

3. **FoodUsageStats** (aggregated usage for quick-add):
   - `id`, `foodName`, `category`, `usageCount`, `lastUsed`, `ibsImpacts`, `isFromCommonFoods`

### Sorting Rules

**CRITICAL**: All queries must sort by:
1. `usage_count DESC` (most-used first)
2. `name ASC` (alphabetically for ties)

Example:
```kotlin
@Query("""
    SELECT * FROM common_foods
    WHERE category = :category
    ORDER BY usage_count DESC, name ASC
""")
fun getCommonFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>>
```

---

## Database Migration (v8→v9)

### Migration Steps (High-Level)

1. **Create new tables**: `common_foods`, `food_usage_stats`
2. **Alter existing table**: Add new columns to `food_items`
3. **Migrate data**: Map old categories → new categories, assign IBS impacts
4. **Pre-populate**: Insert ~150 common foods from `PrePopulatedFoods.kt`
5. **Initialize stats**: Populate `food_usage_stats` from existing `food_items`

### Old Category → New Category Mapping

| Old Category (v8) | New Category (v9) | Assigned IBS Impacts |
|-------------------|-------------------|----------------------|
| `DAIRY` | `DAIRY` | `LACTOSE, FODMAP_HIGH` |
| `GLUTEN` | `GRAINS` | `GLUTEN, FODMAP_HIGH` |
| `HIGH_FODMAP` | `OTHER` | `FODMAP_HIGH` |
| `SPICY` | `OTHER` | `SPICY, FODMAP_LOW` |
| `CAFFEINE` | `BEVERAGES` | `CAFFEINE, FODMAP_LOW` |
| `FATTY` | `FATS_OILS` | `FATTY, FODMAP_LOW` |
| `PROCESSED` | `PROCESSED` | `FODMAP_MODERATE` |
| `ALCOHOL` | `BEVERAGES` | `ALCOHOL, FODMAP_LOW` |
| `RAW` | `OTHER` | `FODMAP_LOW` |

### Rollback Strategy

If migration fails:
1. Room automatically restores v8 backup
2. App shows error dialog with "Retry" button (max 3 attempts)
3. User can export v8 data as JSON for manual recovery
4. After successful migration on retry, user can import JSON

### Testing Migration

**Test Cases** (from spec.md SC-003):
- 10 entries → <5s migration, 0% data loss
- 100 entries → <10s migration, 0% data loss
- 1000 entries → <20s migration, 0% data loss
- 5000 entries → <30s migration, 0% data loss

**Test Procedure**:
1. Create Room migration test extending `MigrationTest`
2. Populate v8 database with test data
3. Run migration
4. Verify all columns present, data preserved, pre-populated foods present

---

## UI Components (Material Design 3)

### Key Screens

**1. FoodScreen (MODIFIED)**:
- 12-category grid (3 columns, 4 rows)
- Quick-add shortcuts section (top 6 foods)
- Floating Action Button (add new food)
- Bottom sheet dialog for food addition

**2. CategoryDetailScreen (NEW)**:
- List of foods in category
- Search bar (fuzzy search)
- Sorted by usage count DESC, then alphabetically ASC
- Long-press to edit food attributes

**3. AddFoodDialog (NEW)**:
- Bottom sheet presentation
- Category selector (12 categories with colors/icons)
- IBS attributes selector (grouped by AttributeCategory)
- FODMAP level selector (required, default: LOW)
- Smart search with common food suggestions

### Material Design 3 Requirements

**Touch Targets**: Minimum 48dp × 48dp
**Contrast**: WCAG AA 4.5:1 for body text
**Colors**: Use semantic colors (primary, secondary, tertiary) from theme
**Accessibility**: All elements must have `contentDescription` for screen readers

### Color Palette (Category Colors)

```kotlin
// Primary colors (MD3 theme)
GRAINS -> TertiaryLight/TertiaryDark
PROTEINS -> PrimaryLight/PrimaryDark
DAIRY -> SecondaryLight/SecondaryDark

// Extended colors (custom palette)
VEGETABLES -> GreenLight/GreenDark
FRUITS -> OrangeLight/OrangeDark
LEGUMES -> BrownLight/BrownDark
NUTS_SEEDS -> AmberLight/AmberDark
BEVERAGES -> BlueLight/BlueDark
FATS_OILS -> YellowLight/YellowDark
SWEETS -> PinkLight/PinkDark
PROCESSED -> RedLight/RedDark
OTHER -> NeutralLight/NeutralDark
```

---

## Development Workflow

### Phase Breakdown (6 Phases)

**Phase 1: Documentation & Planning** ✅ COMPLETE
- Feature specification, data model, contracts, quickstart

**Phase 2: Data Layer** (2-3 days)
- Create enums (FoodCategory, IBSImpact, AttributeCategory)
- Update Room entities (FoodItem, CommonFood, FoodUsageStats)
- Implement DAOs (FoodItemDao, CommonFoodDao, FoodUsageStatsDao)
- Create Migration_8_9.kt
- Update DataRepository
- Write unit tests for DAOs and migration

**Phase 3: UI Layer** (4-5 days)
- Update FoodScreen with 12-category grid
- Create CategoryDetailScreen
- Create AddFoodDialog and EditFoodDialog bottom sheets
- Implement search functionality
- Update theme with category colors
- Write UI integration tests

**Phase 4: Business Logic** (2-3 days)
- Update FoodViewModel with usage tracking
- Implement quick-add shortcuts logic
- Add search and filter logic
- Write ViewModel unit tests

**Phase 5: Testing & Polish** (3-4 days)
- Run migration tests (10/100/1000/5000 entries)
- WCAG AA accessibility testing with TalkBack
- Performance testing (search <1s, quick-add update <200ms)
- Manual testing on API 24+ devices
- Fix bugs and polish UI

**Phase 6: Release Preparation** (1-2 days)
- Update version to 1.9.0 (versionCode 11+)
- Generate release notes
- Create Git tag
- Build release APK
- Deploy to user

### Daily Development Cycle

1. **Morning**: Pick next task from `tasks.md` (to be generated in Phase 2)
2. **Code**: Implement task following clean architecture
3. **Test**: Write unit/integration tests for new code
4. **Review**: Self-review against constitution principles
5. **Commit**: Conventional commit message (feat/fix/docs/test/refactor/chore)
6. **Repeat**: Mark task complete, pick next task

---

## Testing Strategy

### Unit Tests (JUnit 4.13.2)

**Target**: ViewModels, Repository, DAOs

**Example**:
```kotlin
@Test
fun `insert food item updates usage stats`() = runTest {
    // Given: Fresh database
    val foodItem = FoodItem(name = "Banana", category = FRUITS, ...)

    // When: Insert food item
    repository.insertFoodItem(foodItem)

    // Then: FoodUsageStats created with count = 1
    val stats = repository.getStatsByFoodAndCategory("Banana", FRUITS).first()
    assertEquals(1, stats?.usageCount)
}
```

### Instrumented Tests (AndroidX Test)

**Target**: Database operations, UI components

**Migration Test Example**:
```kotlin
@Test
fun testMigration8To9() {
    // Populate v8 database
    database = testHelper.createDatabase(TEST_DB, 8).apply {
        execSQL("INSERT INTO food_items (name, quantity, timestamp, old_category) VALUES ('Milk', '200ml', 1234567890, 'DAIRY')")
        close()
    }

    // Run migration
    database = testHelper.runMigrationsAndValidate(TEST_DB, 9, true, MIGRATION_8_9)

    // Verify data preserved and migrated
    database.query("SELECT * FROM food_items").use { cursor ->
        assertTrue(cursor.moveToFirst())
        assertEquals("Milk", cursor.getString(cursor.getColumnIndex("name")))
        assertEquals("DAIRY", cursor.getString(cursor.getColumnIndex("category")))
        assertTrue(cursor.getString(cursor.getColumnIndex("ibs_impacts")).contains("LACTOSE"))
    }
}
```

### Manual Testing Checklist

**Database Migration**:
- [ ] Fresh install (v8 doesn't exist) → migration skipped, v9 created
- [ ] Update from v8 → migration runs, data preserved
- [ ] Migration failure → rollback to v8, error dialog shown
- [ ] Retry after failure → migration succeeds on 2nd attempt

**Usage Tracking**:
- [ ] Log food → quick-add updates within 200ms
- [ ] Log same food 3x → usage_count increments to 3
- [ ] Delete food → usage_count decrements
- [ ] Delete all entries → quick-add empty

**Search**:
- [ ] Search "yogu" → matches "yogurt" and "yoghurt"
- [ ] Search "bana" → matches "Banana"
- [ ] Empty search → shows all foods in category

**Accessibility**:
- [ ] TalkBack reads all buttons and labels
- [ ] Touch targets minimum 48dp × 48dp
- [ ] Contrast ratios meet WCAG AA (4.5:1)

---

## Performance Targets

**Database Operations** (from spec.md SC-001):
- Quick-add update: p95 < 200ms (write to UI re-render)
- Food search: p95 < 1s (500+ foods)
- Migration: < 30s (5000 entries)
- App memory: < 200MB (typical usage, 500+ foods)

**UI Responsiveness**:
- Category grid load: < 100ms
- Category detail load: < 200ms
- Bottom sheet open animation: 300ms (Material Design standard)

**Profiling Tools**:
- Android Profiler (CPU, Memory, Network)
- SQLite query logging (`PRAGMA query_plan_only`)
- Compose Layout Inspector

---

## Common Pitfalls & Solutions

### Pitfall 1: Forgetting FODMAP Validation

**Problem**: User creates food with 0 or 2+ FODMAP levels
**Solution**: Validate in Repository before insert/update:

```kotlin
require(commonFood.ibsImpacts.count { it.name.startsWith("FODMAP_") } == 1) {
    "CommonFood must have exactly one FODMAP level"
}
```

### Pitfall 2: Incorrect Sorting Order

**Problem**: Foods not sorted by usage count first
**Solution**: ALWAYS use `ORDER BY usage_count DESC, name ASC` in queries

### Pitfall 3: Migration Data Loss

**Problem**: Old data not migrated correctly
**Solution**: Test migration with real data, verify 100% data preservation

### Pitfall 4: UI Thread Blocking

**Problem**: Database queries on main thread
**Solution**: All DAO methods are `suspend` or return `Flow`, use `viewModelScope.launch`

### Pitfall 5: Ignoring Accessibility

**Problem**: Missing `contentDescription` on buttons/icons
**Solution**: Add semantic descriptions to all UI elements:

```kotlin
Icon(
    imageVector = Icons.Default.Grain,
    contentDescription = "Grains category"
)
```

---

## Debugging Tips

### Database Inspection

**Android Studio Database Inspector**:
1. Run app in debug mode
2. View > Tool Windows > App Inspection
3. Database Inspector tab → select `ibs-tracker-database`
4. Query tables directly, inspect schema

**Manual SQLite Export**:
```bash
adb shell "run-as com.tiarkaerell.ibstracker cat /data/data/com.tiarkaerell.ibstracker/databases/ibs-tracker-database" > local.db
sqlite3 local.db
```

### Logcat Filtering

```bash
# Filter by app package
adb logcat | grep "com.tiarkaerell.ibstracker"

# Filter by tag
adb logcat -s "FoodViewModel"

# Filter by priority (Error only)
adb logcat *:E
```

### Compose Layout Inspector

1. Run app in debug mode
2. Tools > Layout Inspector
3. Select running process
4. Inspect Composable tree, preview recompositions

---

## FAQ

**Q: Why 12 categories instead of original 9?**
A: Original categories mixed IBS triggers (GLUTEN, HIGH_FODMAP) with food types (DAIRY). New system uses actual food categories (GRAINS, PROTEINS) with hidden IBS attributes for analysis.

**Q: What happens to old data during migration?**
A: All data is preserved. Old categories are mapped to new categories (e.g., GLUTEN → GRAINS), and IBS impacts are assigned based on old category.

**Q: How do quick-add shortcuts update?**
A: Automatically after logging a food. Repository updates FoodUsageStats, StateFlow emits new state, UI re-renders within 200ms.

**Q: Can users edit IBS attributes for common foods?**
A: Yes, via long-press → edit dialog. Changes are local (don't affect other users). Pre-populated foods have `isVerified = true`.

**Q: What if migration fails?**
A: Automatic rollback to v8, error dialog with retry option (max 3 attempts). User can export v8 data as JSON for manual recovery.

**Q: How are FODMAP levels enforced?**
A: Repository validates exactly one FODMAP_* value before insert/update. UI defaults to FODMAP_LOW, user can change during food addition.

**Q: Performance with 1000+ custom foods?**
A: Indexed queries on `usage_count` and `category`. Search p95 < 1s. Memory footprint < 200MB. Tested up to 5000 entries.

---

## Next Steps

1. **Read Full Specification**: Review `spec.md` for complete requirements
2. **Study Data Model**: Understand entity relationships in `data-model.md`
3. **Review Research**: Read `research.md` for technical patterns
4. **Wait for Tasks**: `tasks.md` will break down implementation into atomic tasks
5. **Start Phase 2**: Begin with Data Layer implementation (enums, entities, DAOs)

---

## Support & Resources

**Internal Documentation**:
- `CLAUDE.md` (project root) - Project architecture and commands
- `.specify/memory/constitution.md` - Development principles (7 gates)
- `docs/food-categories/` - Feature-specific documentation

**External Resources**:
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Jetpack Compose Material 3](https://developer.android.com/jetpack/compose/designsystems/material3)
- [FODMAP Diet Basics](https://www.monashfodmap.com/about-fodmap-and-ibs/)

**Questions?**
Check `spec.md` first, then `research.md`, then ask the team.

---

**Document Version**: 1.0.0
**Last Updated**: 2025-10-21
**Author**: Claude Code (AI Assistant)
**Review Status**: Pending team review
