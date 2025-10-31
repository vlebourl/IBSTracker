# Data Model: Fix Custom Food Addition Bug

**Feature**: 004-fix-custom-food-persistence
**Date**: 2025-10-27
**Status**: Complete

## Overview

This feature does not require any schema changes. All necessary fields already exist in database schema v9. This document describes how existing entities are used to fix the custom food persistence bug.

---

## Entities

### CommonFood (Existing - No Changes)

**Table**: `common_foods`
**Purpose**: Stores both pre-populated and user-added custom foods

**Schema** (existing):
```kotlin
@Entity(tableName = "common_foods")
data class CommonFood(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,                    // Food name (e.g., "Soja")

    @ColumnInfo(name = "category")
    val category: FoodCategory,          // One of 12 categories

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact>,     // IBS attributes (FODMAP, triggers, properties)

    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,             // Number of times logged

    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = false,     // true = pre-populated, false = custom

    @ColumnInfo(name = "search_terms")
    val searchTerms: String = "",        // Alternative names for fuzzy matching

    @ColumnInfo(name = "name_en")
    val nameEn: String = "",             // English translation (optional)

    @ColumnInfo(name = "name_fr")
    val nameFr: String = ""              // French translation (optional)
)
```

**Key Fields for This Feature**:
- `is_verified`: Set to `false` for custom foods (distinguishes from pre-populated)
- `name`: Used for duplicate detection (exact match)
- `category`: Inherited from user's selected category
- `ibsImpacts`: Defaulted to `listOf(IBSImpact.FODMAP_LOW)` for custom foods
- `usageCount`: Starts at 0, incremented when food is logged

**Constraints**:
- `name` must be unique per category (Room enforces via query, not DB constraint)
- `ibsImpacts` must contain exactly one FODMAP level (validated in repository)

---

### FoodItem (Existing - No Changes)

**Table**: `food_items`
**Purpose**: Stores individual food logging events

**Schema** (existing):
```kotlin
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,                    // Food name at time of logging

    @ColumnInfo(name = "quantity")
    val quantity: String = "",           // Optional quantity (e.g., "1 cup")

    @ColumnInfo(name = "timestamp")
    val timestamp: Date = Date(),        // When food was logged

    @ColumnInfo(name = "category")
    val category: FoodCategory = FoodCategory.OTHER,

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact> = emptyList(),

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,       // Deprecated: Use commonFoodId instead

    @ColumnInfo(name = "common_food_id")
    val commonFoodId: Long? = null       // Foreign key to CommonFood (nullable for old entries)
)
```

**Key Fields for This Feature**:
- `commonFoodId`: **This is the critical field** - links FoodItem to CommonFood
  - Previously: Always `null` for custom foods (bug!)
  - Fixed: Set to `CommonFood.id` when food is created/found
- `isCustom`: Deprecated in favor of checking `CommonFood.is_verified`

**Backward Compatibility**:
- Old FoodItems with `commonFoodId = null` continue working
- New FoodItems always have `commonFoodId` set
- No migration required

---

### FoodUsageStats (Existing - No Changes)

**Table**: `food_usage_stats`
**Purpose**: Tracks usage statistics per food name + category combination

**Schema** (existing):
```kotlin
@Entity(
    tableName = "food_usage_stats",
    primaryKeys = ["food_name", "category"]
)
data class FoodUsageStats(
    @ColumnInfo(name = "food_name")
    val foodName: String,

    @ColumnInfo(name = "category")
    val category: FoodCategory,

    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 1,

    @ColumnInfo(name = "last_used")
    val lastUsed: Date = Date(),

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact> = emptyList(),

    @ColumnInfo(name = "is_from_common_foods")
    val isFromCommonFoods: Boolean = false
)
```

**Key Fields for This Feature**:
- `isFromCommonFoods`: Set to `true` when custom food is created
- `usageCount`: Auto-incremented by existing `upsertUsageStats()` logic
- No changes needed - existing logic already handles custom foods correctly

---

## Entity Relationships

```text
CommonFood (1) ────┐
                   │
                   │ commonFoodId (nullable FK)
                   │
                   ├──> FoodItem (N)
                   │
                   │ usage tracking
                   │
                   └──> FoodUsageStats (1 per category)

Legend:
- (1) = one instance
- (N) = many instances
- Solid line = foreign key relationship
- Dotted line = logical relationship (no FK constraint)
```

**Relationship Details**:

1. **CommonFood → FoodItem** (One-to-Many):
   - One CommonFood can have many FoodItems (logging events)
   - FoodItem.commonFoodId references CommonFood.id
   - Nullable FK allows old FoodItems without CommonFood reference

2. **CommonFood → FoodUsageStats** (Logical):
   - Each CommonFood contributes to FoodUsageStats for its category
   - Updated automatically when FoodItem is created
   - No direct FK relationship (composite key on name + category)

---

## Data Flow

### Creating a Custom Food

```text
User Input (FoodScreen)
    ↓
FoodViewModel.saveFoodItem(name, category)
    ↓
DataRepository.insertFoodItem(foodItem)
    ↓
┌─────────────────────────────────────┐
│ 1. Check for existing CommonFood    │
│    getCommonFoodByName(name).first()│
└─────────────────────────────────────┘
    ↓
  Exists?
    ├─ YES → Use existing CommonFood.id
    │
    └─ NO → Create new CommonFood
            ┌────────────────────────────┐
            │ CommonFood(                │
            │   name = foodItem.name,    │
            │   category = foodItem.cat, │
            │   ibsImpacts = [FODMAP_LOW]│
            │   isVerified = false,      │
            │   usageCount = 0           │
            │ )                          │
            └────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ 2. Insert FoodItem with             │
│    commonFoodId = CommonFood.id     │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ 3. Increment CommonFood.usage_count │
│    (if linked)                      │
└─────────────────────────────────────┘
    ↓
┌─────────────────────────────────────┐
│ 4. Upsert FoodUsageStats            │
│    (existing logic, no changes)     │
└─────────────────────────────────────┘
    ↓
UI auto-updates via Flow
```

### Displaying Category Foods

```text
FoodScreen.CategoryView
    ↓
FoodViewModel.getCommonFoodsByCategory(category)
    ↓
DataRepository.getCommonFoodsByCategory(category)
    ↓
CommonFoodDao.getCommonFoodsByCategory(category)
    ↓
Query: SELECT * FROM common_foods
       WHERE category = :category
       ORDER BY usage_count DESC, name ASC
    ↓
Flow<List<CommonFood>> (includes both verified + custom)
    ↓
UI displays top 6 foods (existing logic)
```

---

## Validation Rules

### CommonFood Validation

Enforced in `DataRepository.insertCommonFood()`:

1. **FODMAP Level**: Exactly one FODMAP level required
   ```kotlin
   val fodmapCount = commonFood.ibsImpacts.count { it.name.startsWith("FODMAP_") }
   require(fodmapCount == 1) { "CommonFood must have exactly one FODMAP level" }
   ```

2. **Name**: Non-empty string
   - Validated by UI (TextField requires non-blank input)
   - Room enforces non-null via schema

3. **Category**: Valid FoodCategory enum value
   - Type-safe: Kotlin enum prevents invalid values

### FoodItem Validation

Enforced in `FoodViewModel.saveFoodItem()`:

1. **Name**: Non-blank string
   ```kotlin
   if (customFoodName.isNotBlank()) { ... }
   ```

2. **Timestamp**: Not in future
   - UI provides DateTimePicker (defaults to current time)
   - No explicit validation (users can log past meals)

---

## State Transitions

### CommonFood Lifecycle

```text
┌─────────────┐
│   Created   │  (isVerified = false, usageCount = 0)
│  (Custom)   │
└─────────────┘
       ↓
       │ First FoodItem logged
       ↓
┌─────────────┐
│  In Use     │  (usageCount increments with each log)
└─────────────┘
       ↓
       │ User stops logging food
       ↓
┌─────────────┐
│  Inactive   │  (usageCount > 0, but no recent logs)
└─────────────┘
       ↓
       │ (Future: Auto-archive after 12 months?)
       ↓
┌─────────────┐
│  Archived   │  (Moved to separate table - out of scope)
└─────────────┘
```

**Notes**:
- No "verified" state transition (custom foods remain `isVerified = false`)
- No deletion state (deletion not implemented in MVP)
- Usage count only increases (no decrement on FoodItem delete)

---

## Indexing Strategy

### Current Indexes (Schema v9)

Room automatically indexes:
- Primary keys: `common_foods.id`, `food_items.id`
- Foreign keys: None (commonFoodId is nullable, no FK constraint)

### Recommended Future Indexes

For performance with 200+ custom foods:

1. **common_foods.name** (for duplicate detection):
   ```sql
   CREATE INDEX idx_common_foods_name ON common_foods(name);
   ```
   - Benefit: O(log n) lookup instead of O(n) table scan
   - Impact: < 1ms for 500 foods

2. **common_foods.category** (for category filtering):
   ```sql
   CREATE INDEX idx_common_foods_category ON common_foods(category);
   ```
   - Benefit: Faster category list queries
   - Already fast due to small dataset (12 categories × 6 foods)

**Decision**: Defer indexing to future migration (current perf acceptable)

---

## Database Queries (Existing - No Changes)

### Duplicate Detection

```kotlin
@Query("SELECT * FROM common_foods WHERE name = :name")
fun getCommonFoodByName(name: String): Flow<CommonFood?>
```

**Usage**: Check if custom food already exists before creating

---

### Category Food List

```kotlin
@Query("""
    SELECT * FROM common_foods
    WHERE category = :category
    ORDER BY usage_count DESC, name ASC
""")
fun getCommonFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>>
```

**Usage**: Display foods in category detail screen (includes custom foods)

---

### Search Foods

```kotlin
@Query("""
    SELECT * FROM common_foods
    WHERE name LIKE '%' || :query || '%'
       OR search_terms LIKE '%' || :query || '%'
    ORDER BY usage_count DESC, name ASC
    LIMIT 50
""")
fun searchCommonFoods(query: String): Flow<List<CommonFood>>
```

**Usage**: Search bar (includes custom foods automatically)

---

### Usage Count Update

```kotlin
@Query("""
    UPDATE common_foods
    SET usage_count = usage_count + 1
    WHERE id = :id
""")
suspend fun incrementUsageCountById(id: Long): Int
```

**Usage**: Called after FoodItem insert (if commonFoodId not null)

---

## Migration Strategy

**Status**: ✅ No migration required

**Reasoning**:
- Schema v9 already has all needed columns:
  - `common_foods.is_verified` (Boolean)
  - `food_items.common_food_id` (Long?, nullable)
  - `common_foods.usage_count` (Int)
- No new tables
- No column additions/modifications
- No data backfill needed (nullable FK handles old data)

**Future Migration (Out of Scope)**:
- Backfill `commonFoodId` for old FoodItems
- Add indexes on `common_foods.name` and `category`
- Clean up deprecated `food_items.is_custom` column

---

## Data Examples

### Pre-populated CommonFood (Existing)

```kotlin
CommonFood(
    id = 1,
    name = "Riz blanc",
    category = FoodCategory.GRAINS_STARCHES,
    ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
    usageCount = 15,            // Incremented over time
    isVerified = true,          // Pre-populated
    searchTerms = "rice, white rice, riz",
    nameEn = "White rice",
    nameFr = "Riz blanc"
)
```

### Custom CommonFood (New)

```kotlin
CommonFood(
    id = 73,                    // Auto-generated
    name = "Soja",              // User-entered
    category = FoodCategory.OTHER,  // Selected by user
    ibsImpacts = listOf(IBSImpact.FODMAP_LOW),  // Default
    usageCount = 0,             // Initial value
    isVerified = false,         // Custom food
    searchTerms = "",           // Empty for custom
    nameEn = "",                // Empty for custom
    nameFr = ""                 // Empty for custom
)
```

### FoodItem (Linked to Custom Food)

```kotlin
FoodItem(
    id = 1234,
    name = "Soja",
    quantity = "1 cup",
    timestamp = Date(),
    category = FoodCategory.OTHER,
    ibsImpacts = listOf(IBSImpact.FODMAP_LOW),  // Copied from CommonFood
    isCustom = false,           // Deprecated field
    commonFoodId = 73           // ✅ LINKED to CommonFood
)
```

### FoodUsageStats (Auto-created)

```kotlin
FoodUsageStats(
    foodName = "Soja",
    category = FoodCategory.OTHER,
    usageCount = 1,
    lastUsed = Date(),
    ibsImpacts = listOf(IBSImpact.FODMAP_LOW),
    isFromCommonFoods = true    // ✅ Indicates from CommonFood
)
```

---

## Summary

**Schema Changes**: None
**New Entities**: None
**Modified Entities**: None
**New Queries**: None
**Modified Queries**: None

**Only Code Change**: `DataRepository.insertFoodItem()` business logic
- Add duplicate detection
- Create CommonFood if not found
- Link FoodItem via commonFoodId
- ~15 lines of code

This is a **minimal, surgical fix** that leverages existing database schema without requiring migrations.
