# Data Model: Smart Food Categorization System

**Branch**: `001-smart-food-categorization` | **Date**: 2025-10-21 | **Spec**: [spec.md](./spec.md)

## Overview

This document defines the complete data model for the Smart Food Categorization feature, including all entities, enums, relationships, and validation rules. The model supports migration from database v8→v9 with automatic rollback and pre-population of ~150 common foods.

## Entity Definitions

### 1. FoodCategory (Enum)

**Purpose**: Actual food type categories (not IBS-trigger based)

**Type**: Kotlin sealed class / enum

**Values** (12 categories):

| Category | Color (MD3) | Icon | Rationale |
|----------|-------------|------|-----------|
| `GRAINS` | Tertiary/TertiaryContainer | `grain` | Bread, pasta, rice, cereals |
| `PROTEINS` | Primary/PrimaryContainer | `restaurant` | Meat, fish, poultry, eggs |
| `DAIRY` | Secondary/SecondaryContainer | `icecream` | Milk, cheese, yogurt, butter |
| `VEGETABLES` | Green (MD3 extended) | `eco` | All vegetables including leafy greens |
| `FRUITS` | Orange (MD3 extended) | `apple` | Fresh, dried, canned fruits |
| `LEGUMES` | Brown (MD3 extended) | `spa` | Beans, lentils, chickpeas, peas |
| `NUTS_SEEDS` | Amber (MD3 extended) | `nature` | Tree nuts, peanuts, seeds |
| `BEVERAGES` | Blue (MD3 extended) | `local_drink` | Coffee, tea, juice, alcohol |
| `FATS_OILS` | Yellow (MD3 extended) | `water_drop` | Cooking oils, butter, margarine |
| `SWEETS` | Pink (MD3 extended) | `cake` | Desserts, candy, sweeteners |
| `PROCESSED` | Red (MD3 extended) | `fastfood` | Pre-packaged, fast food |
| `OTHER` | Neutral/SurfaceVariant | `category` | Miscellaneous items |

**Fields**:
```kotlin
enum class FoodCategory(
    val displayName: String,
    val displayNameFr: String,
    val colorLight: Color,
    val colorDark: Color,
    val icon: ImageVector,
    val sortOrder: Int
) {
    GRAINS("Grains", "Céréales", TertiaryLight, TertiaryDark, Icons.Default.Grain, 1),
    PROTEINS("Proteins", "Protéines", PrimaryLight, PrimaryDark, Icons.Default.Restaurant, 2),
    DAIRY("Dairy", "Produits laitiers", SecondaryLight, SecondaryDark, Icons.Default.Icecream, 3),
    VEGETABLES("Vegetables", "Légumes", GreenLight, GreenDark, Icons.Default.Eco, 4),
    FRUITS("Fruits", "Fruits", OrangeLight, OrangeDark, Icons.Default.Apple, 5),
    LEGUMES("Legumes", "Légumineuses", BrownLight, BrownDark, Icons.Default.Spa, 6),
    NUTS_SEEDS("Nuts & Seeds", "Noix et graines", AmberLight, AmberDark, Icons.Default.Nature, 7),
    BEVERAGES("Beverages", "Boissons", BlueLight, BlueDark, Icons.Default.LocalDrink, 8),
    FATS_OILS("Fats & Oils", "Matières grasses", YellowLight, YellowDark, Icons.Default.WaterDrop, 9),
    SWEETS("Sweets", "Sucreries", PinkLight, PinkDark, Icons.Default.Cake, 10),
    PROCESSED("Processed Foods", "Aliments transformés", RedLight, RedDark, Icons.Default.Fastfood, 11),
    OTHER("Other", "Autre", NeutralLight, NeutralDark, Icons.Default.Category, 12);
}
```

**Validation Rules**:
- NOT NULL (required field)
- Stored as TEXT in Room database
- TypeConverter: `FoodCategory.name` ↔ `String`

---

### 2. IBSImpact (Enum)

**Purpose**: Hidden IBS impact attributes for analytical purposes (11 attributes)

**Type**: Kotlin sealed class / enum

**Grouped by Category** (from IBS_ATTRIBUTES.md):

| Attribute | Group | Display Name | Description |
|-----------|-------|--------------|-------------|
| `FODMAP_HIGH` | FODMAP | High FODMAP | High fermentable carbs (triggers gas/bloating) |
| `FODMAP_MODERATE` | FODMAP | Moderate FODMAP | Moderate fermentable carbs (portion-dependent) |
| `FODMAP_LOW` | FODMAP | Low FODMAP | Low fermentable carbs (generally safe) |
| `GLUTEN` | Grain-Based | Contains Gluten | Wheat, barley, rye proteins |
| `LACTOSE` | Dairy-Based | Contains Lactose | Milk sugar (triggers lactose intolerance) |
| `CAFFEINE` | Stimulants | Contains Caffeine | Stimulant affecting gut motility |
| `ALCOHOL` | Stimulants | Contains Alcohol | Irritates gut lining, affects motility |
| `SPICY` | Irritants | Spicy/Hot | Capsaicin triggers pain receptors |
| `FATTY` | Macronutrients | High Fat | Slows digestion, triggers gallbladder |
| `ACIDIC` | Chemical | Acidic | Low pH irritates gut lining |
| `ARTIFICIAL_SWEETENERS` | Additives | Artificial Sweeteners | Sugar alcohols, aspartame (osmotic effect) |

**Fields**:
```kotlin
enum class IBSImpact(
    val displayName: String,
    val displayNameFr: String,
    val category: AttributeCategory,
    val description: String,
    val icon: ImageVector,
    val colorLight: Color,
    val colorDark: Color
) {
    FODMAP_HIGH("High FODMAP", "FODMAP élevé", AttributeCategory.FODMAP, "High fermentable carbs", Icons.Default.Warning, ErrorLight, ErrorDark),
    FODMAP_MODERATE("Moderate FODMAP", "FODMAP modéré", AttributeCategory.FODMAP, "Moderate fermentable carbs", Icons.Default.Info, WarningLight, WarningDark),
    FODMAP_LOW("Low FODMAP", "FODMAP faible", AttributeCategory.FODMAP, "Low fermentable carbs", Icons.Default.CheckCircle, SuccessLight, SuccessDark),
    GLUTEN("Contains Gluten", "Contient du gluten", AttributeCategory.GRAIN_BASED, "Wheat, barley, rye", Icons.Default.Grain, ErrorLight, ErrorDark),
    LACTOSE("Contains Lactose", "Contient du lactose", AttributeCategory.DAIRY_BASED, "Milk sugar", Icons.Default.Icecream, ErrorLight, ErrorDark),
    CAFFEINE("Contains Caffeine", "Contient de la caféine", AttributeCategory.STIMULANTS, "Stimulant", Icons.Default.LocalCafe, WarningLight, WarningDark),
    ALCOHOL("Contains Alcohol", "Contient de l'alcool", AttributeCategory.STIMULANTS, "Alcohol", Icons.Default.LocalBar, ErrorLight, ErrorDark),
    SPICY("Spicy/Hot", "Épicé/Piquant", AttributeCategory.IRRITANTS, "Capsaicin", Icons.Default.LocalFireDepartment, WarningLight, WarningDark),
    FATTY("High Fat", "Riche en graisses", AttributeCategory.MACRONUTRIENTS, "High fat content", Icons.Default.WaterDrop, WarningLight, WarningDark),
    ACIDIC("Acidic", "Acide", AttributeCategory.CHEMICAL, "Low pH", Icons.Default.Science, WarningLight, WarningDark),
    ARTIFICIAL_SWEETENERS("Artificial Sweeteners", "Édulcorants artificiels", AttributeCategory.ADDITIVES, "Sugar alcohols", Icons.Default.Cancel, ErrorLight, ErrorDark);
}
```

**Validation Rules**:
- Stored as JSON array in Room database: `TEXT NOT NULL DEFAULT '[]'`
- TypeConverter: `List<IBSImpact>` ↔ `String` (JSON array of enum names)
- Can be empty list (user skips during food addition)
- FODMAP level is special: exactly one FODMAP_* value should be present (default: FODMAP_LOW)

---

### 3. AttributeCategory (Enum)

**Purpose**: Grouping for IBS impact attributes (UI organization)

**Type**: Kotlin sealed class / enum

**Values**:
```kotlin
enum class AttributeCategory(
    val displayName: String,
    val displayNameFr: String,
    val icon: ImageVector,
    val description: String
) {
    FODMAP("FODMAP Level", "Niveau FODMAP", Icons.Default.Nutrition, "Fermentable carbohydrate content"),
    GRAIN_BASED("Grain-Based", "Céréales", Icons.Default.Grain, "Grain-related triggers"),
    DAIRY_BASED("Dairy-Based", "Produits laitiers", Icons.Default.Icecream, "Dairy-related triggers"),
    STIMULANTS("Stimulants", "Stimulants", Icons.Default.LocalCafe, "Caffeine and alcohol"),
    IRRITANTS("Irritants", "Irritants", Icons.Default.LocalFireDepartment, "Spicy and hot foods"),
    MACRONUTRIENTS("Macronutrients", "Macronutriments", Icons.Default.FitnessCenter, "Fat, fiber, protein content"),
    CHEMICAL("Chemical Properties", "Propriétés chimiques", Icons.Default.Science, "Acidity and pH"),
    ADDITIVES("Additives", "Additifs", Icons.Default.Cancel, "Artificial ingredients");
}
```

---

### 4. FoodItem (Entity - MODIFIED)

**Purpose**: User-logged food entries with timestamps and IBS attributes

**Table Name**: `food_items`

**Schema Changes** (v8→v9 migration):
- ✅ Existing: `id`, `name`, `quantity`, `timestamp`, `old_category`
- ➕ New: `category` (FoodCategory), `ibs_impacts` (List<IBSImpact>), `is_custom` (Boolean), `common_food_id` (Long?)

**Fields**:
```kotlin
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity")
    val quantity: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Date,

    @ColumnInfo(name = "category")
    val category: FoodCategory,  // NEW: Replaced old_category

    @ColumnInfo(name = "ibs_impacts", defaultValue = "[]")
    val ibsImpacts: List<IBSImpact>,  // NEW: JSON array

    @ColumnInfo(name = "is_custom", defaultValue = "1")
    val isCustom: Boolean = true,  // NEW: true = user-added, false = from common_foods

    @ColumnInfo(name = "common_food_id")
    val commonFoodId: Long? = null  // NEW: FK to common_foods (nullable)
)
```

**Validation Rules**:
- `name`: NOT NULL, min length 1, max length 100
- `quantity`: NOT NULL, alphanumeric + units (e.g., "200g", "1 cup")
- `timestamp`: NOT NULL, Date type (TypeConverter to Long)
- `category`: NOT NULL, valid FoodCategory enum value
- `ibsImpacts`: NOT NULL, can be empty list `[]`, must contain valid IBSImpact values
- `isCustom`: NOT NULL, default true
- `commonFoodId`: NULLABLE, foreign key to `common_foods.id` (not enforced in Room)

**Relationships**:
- **One-to-Many**: CommonFood → FoodItem (via `commonFoodId`)
- **Implicit**: FoodItem → FoodUsageStats (via `name` + `category` matching)

**Indices**:
```kotlin
@Entity(
    tableName = "food_items",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["category"]),
        Index(value = ["common_food_id"])
    ]
)
```

---

### 5. CommonFood (Entity - NEW)

**Purpose**: Pre-populated database of ~150 common foods with verified IBS attributes

**Table Name**: `common_foods`

**Fields**:
```kotlin
@Entity(tableName = "common_foods")
data class CommonFood(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,  // English name (primary key for search)

    @ColumnInfo(name = "category")
    val category: FoodCategory,

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact>,

    @ColumnInfo(name = "search_terms")
    val searchTerms: List<String>,  // Aliases for fuzzy search (e.g., ["yogurt", "yoghurt"])

    @ColumnInfo(name = "usage_count", defaultValue = "0")
    val usageCount: Int = 0,  // Incremented when user logs this food

    @ColumnInfo(name = "name_fr")
    val nameFr: String? = null,  // French translation (optional)

    @ColumnInfo(name = "name_en")
    val nameEn: String? = null,  // Explicit English name (optional, for clarity)

    @ColumnInfo(name = "is_verified", defaultValue = "1")
    val isVerified: Boolean = true,  // true = pre-populated, false = user-added to common_foods

    @ColumnInfo(name = "created_at")
    val createdAt: Date
)
```

**Validation Rules**:
- `name`: NOT NULL, UNIQUE, min length 1, max length 100
- `category`: NOT NULL, valid FoodCategory enum value
- `ibsImpacts`: NOT NULL, MUST contain exactly one FODMAP_* value (enforced in DAO)
- `searchTerms`: NOT NULL, can be empty list, stored as JSON array
- `usageCount`: NOT NULL, default 0, integer >= 0
- `nameFr`, `nameEn`: NULLABLE, max length 100
- `isVerified`: NOT NULL, default true
- `createdAt`: NOT NULL, Date type (TypeConverter to Long)

**Indices**:
```kotlin
@Entity(
    tableName = "common_foods",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["category"]),
        Index(value = ["usage_count"])
    ]
)
```

**Pre-population**:
- ~150 foods loaded during Migration_8_9 from `PrePopulatedFoods.kt`
- All pre-populated foods have `isVerified = true`
- Usage counts start at 0, updated when user logs food

---

### 6. FoodUsageStats (Entity - NEW)

**Purpose**: Aggregated usage statistics for quick-add shortcuts (top 6 most-used foods)

**Table Name**: `food_usage_stats`

**Fields**:
```kotlin
@Entity(tableName = "food_usage_stats")
data class FoodUsageStats(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "food_name")
    val foodName: String,  // Matches FoodItem.name or CommonFood.name

    @ColumnInfo(name = "category")
    val category: FoodCategory,

    @ColumnInfo(name = "usage_count")
    val usageCount: Int,  // Number of times logged

    @ColumnInfo(name = "last_used")
    val lastUsed: Date,  // Most recent timestamp

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact>,  // Cached from CommonFood or last FoodItem

    @ColumnInfo(name = "is_from_common_foods", defaultValue = "0")
    val isFromCommonFoods: Boolean = false  // true if matches CommonFood, false if custom
)
```

**Validation Rules**:
- `foodName`: NOT NULL, min length 1, max length 100
- `category`: NOT NULL, valid FoodCategory enum value
- `usageCount`: NOT NULL, integer > 0 (entries with 0 usage are deleted)
- `lastUsed`: NOT NULL, Date type (TypeConverter to Long)
- `ibsImpacts`: NOT NULL, can be empty list
- `isFromCommonFoods`: NOT NULL, default false

**Indices**:
```kotlin
@Entity(
    tableName = "food_usage_stats",
    indices = [
        Index(value = ["usage_count"]),
        Index(value = ["category"]),
        Index(value = ["last_used"])
    ]
)
```

**Update Strategy**:
- Auto-updated via DAO trigger after FoodItem insert
- Sorted by `usage_count DESC, foodName ASC` for UI display
- Top 6 results used for quick-add shortcuts

---

## Type Converters

### DateConverter (EXISTING)
```kotlin
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

### FoodCategoryConverter (NEW)
```kotlin
class Converters {
    @TypeConverter
    fun fromFoodCategory(value: FoodCategory): String {
        return value.name
    }

    @TypeConverter
    fun toFoodCategory(value: String): FoodCategory {
        return FoodCategory.valueOf(value)
    }
}
```

### IBSImpactListConverter (NEW)
```kotlin
class Converters {
    @TypeConverter
    fun fromIBSImpactList(value: List<IBSImpact>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toIBSImpactList(value: String): List<IBSImpact> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { IBSImpact.valueOf(it) }
    }
}
```

### StringListConverter (NEW)
```kotlin
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString("|")  // Use pipe separator to avoid conflicts
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isEmpty()) return emptyList()
        return value.split("|")
    }
}
```

---

## Entity Relationships

### Diagram
```
CommonFood (1) ──────── (*) FoodItem
    │                       │
    │ (name + category)     │ (triggers update)
    │                       │
    └────────── (implicit) ─┴──> FoodUsageStats (1)
```

### Relationship Details

1. **CommonFood → FoodItem** (One-to-Many):
   - Foreign Key: `FoodItem.commonFoodId` → `CommonFood.id`
   - Cascade: None (Room doesn't enforce FK constraints)
   - Nullability: `commonFoodId` is nullable (custom foods have null)

2. **FoodItem → FoodUsageStats** (Implicit):
   - No foreign key relationship (updated via DAO trigger)
   - Match logic: `FoodItem.name == FoodUsageStats.foodName` AND `FoodItem.category == FoodUsageStats.category`
   - Update strategy: Increment `usageCount`, update `lastUsed` on FoodItem insert

3. **CommonFood → FoodUsageStats** (Implicit):
   - No foreign key relationship
   - Match logic: `CommonFood.name == FoodUsageStats.foodName`
   - Update strategy: Sync `usageCount` from CommonFood on query

---

## Migration Strategy (v8→v9)

### Migration Steps

**Step 1: Create New Tables**
```sql
CREATE TABLE IF NOT EXISTS `common_foods` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `name` TEXT NOT NULL,
    `category` TEXT NOT NULL,
    `ibs_impacts` TEXT NOT NULL,
    `search_terms` TEXT NOT NULL,
    `usage_count` INTEGER NOT NULL DEFAULT 0,
    `name_fr` TEXT,
    `name_en` TEXT,
    `is_verified` INTEGER NOT NULL DEFAULT 1,
    `created_at` INTEGER NOT NULL
);

CREATE UNIQUE INDEX `index_common_foods_name` ON `common_foods` (`name`);
CREATE INDEX `index_common_foods_category` ON `common_foods` (`category`);
CREATE INDEX `index_common_foods_usage_count` ON `common_foods` (`usage_count`);

CREATE TABLE IF NOT EXISTS `food_usage_stats` (
    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `food_name` TEXT NOT NULL,
    `category` TEXT NOT NULL,
    `usage_count` INTEGER NOT NULL,
    `last_used` INTEGER NOT NULL,
    `ibs_impacts` TEXT NOT NULL,
    `is_from_common_foods` INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX `index_food_usage_stats_usage_count` ON `food_usage_stats` (`usage_count`);
CREATE INDEX `index_food_usage_stats_category` ON `food_usage_stats` (`category`);
CREATE INDEX `index_food_usage_stats_last_used` ON `food_usage_stats` (`last_used`);
```

**Step 2: Alter Existing Table**
```sql
-- Add new columns with defaults
ALTER TABLE food_items ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER';
ALTER TABLE food_items ADD COLUMN ibs_impacts TEXT NOT NULL DEFAULT '[]';
ALTER TABLE food_items ADD COLUMN is_custom INTEGER NOT NULL DEFAULT 1;
ALTER TABLE food_items ADD COLUMN common_food_id INTEGER;

-- Create indices on new columns
CREATE INDEX `index_food_items_category` ON `food_items` (`category`);
CREATE INDEX `index_food_items_common_food_id` ON `food_items` (`common_food_id`);
```

**Step 3: Migrate Existing Data**
```sql
-- Map old categories to new categories (from DATABASE_SCHEMA.md)
UPDATE food_items
SET category = CASE old_category
    WHEN 'DAIRY' THEN 'DAIRY'
    WHEN 'GLUTEN' THEN 'GRAINS'
    WHEN 'HIGH_FODMAP' THEN 'OTHER'
    WHEN 'SPICY' THEN 'OTHER'
    WHEN 'CAFFEINE' THEN 'BEVERAGES'
    WHEN 'FATTY' THEN 'FATS_OILS'
    WHEN 'PROCESSED' THEN 'PROCESSED'
    WHEN 'RAW' THEN 'OTHER'
    WHEN 'ALCOHOL' THEN 'BEVERAGES'
    ELSE 'OTHER'
END;

-- Assign IBS impacts based on old category
UPDATE food_items
SET ibs_impacts = CASE old_category
    WHEN 'DAIRY' THEN 'LACTOSE,FODMAP_HIGH'
    WHEN 'GLUTEN' THEN 'GLUTEN,FODMAP_HIGH'
    WHEN 'HIGH_FODMAP' THEN 'FODMAP_HIGH'
    WHEN 'SPICY' THEN 'SPICY,FODMAP_LOW'
    WHEN 'CAFFEINE' THEN 'CAFFEINE,FODMAP_LOW'
    WHEN 'FATTY' THEN 'FATTY,FODMAP_LOW'
    WHEN 'PROCESSED' THEN 'FODMAP_MODERATE'
    WHEN 'ALCOHOL' THEN 'ALCOHOL,FODMAP_LOW'
    ELSE 'FODMAP_LOW'
END;

-- Drop old column
ALTER TABLE food_items DROP COLUMN old_category;
```

**Step 4: Pre-populate Common Foods**
```kotlin
// Executed in Migration_8_9.kt using compiled statement
val stmt = database.compileStatement(
    "INSERT INTO common_foods (name, category, ibs_impacts, search_terms, created_at) VALUES (?, ?, ?, ?, ?)"
)
PrePopulatedFoods.foods.forEach { food ->
    stmt.clearBindings()
    stmt.bindString(1, food.name)
    stmt.bindString(2, food.category.name)
    stmt.bindString(3, food.ibsImpacts.joinToString(",") { it.name })
    stmt.bindString(4, food.searchTerms.joinToString("|"))
    stmt.bindLong(5, System.currentTimeMillis())
    stmt.executeInsert()
}
```

**Step 5: Initialize Usage Stats**
```sql
-- Populate food_usage_stats from existing food_items
INSERT INTO food_usage_stats (food_name, category, usage_count, last_used, ibs_impacts, is_from_common_foods)
SELECT
    name,
    category,
    COUNT(*) as usage_count,
    MAX(timestamp) as last_used,
    ibs_impacts,
    0 as is_from_common_foods
FROM food_items
GROUP BY name, category
HAVING COUNT(*) > 0;
```

### Rollback Strategy

If migration fails at any step:
1. Room automatically restores database from v8 backup
2. App shows error dialog with retry option (max 3 attempts)
3. User can export v8 data as JSON for manual recovery
4. User can import JSON after successful migration on retry

---

## Validation & Constraints

### Application-Level Validation

**FoodItem Validation** (in ViewModel/Repository):
```kotlin
fun validateFoodItem(item: FoodItem): ValidationResult {
    if (item.name.isBlank()) return ValidationResult.Error("Name cannot be empty")
    if (item.name.length > 100) return ValidationResult.Error("Name too long (max 100 chars)")
    if (item.quantity.isBlank()) return ValidationResult.Error("Quantity required")
    if (item.ibsImpacts.count { it.name.startsWith("FODMAP_") } != 1) {
        return ValidationResult.Error("Exactly one FODMAP level required")
    }
    return ValidationResult.Success
}
```

**CommonFood Validation** (in DAO/Repository):
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertCommonFood(food: CommonFood): Long {
    // Validate FODMAP constraint
    require(food.ibsImpacts.count { it.name.startsWith("FODMAP_") } == 1) {
        "CommonFood must have exactly one FODMAP level"
    }
    return dao.insert(food)
}
```

### Database-Level Constraints

**Unique Constraints**:
- `common_foods.name` UNIQUE (enforced via index)

**Check Constraints** (not enforced by Room, validated in app):
- `usage_count >= 0` (both CommonFood and FoodUsageStats)
- `ibsImpacts` contains valid enum values (enforced by TypeConverter)
- `category` is valid FoodCategory enum value (enforced by TypeConverter)

---

## Performance Considerations

### Query Optimization

**Quick-Add Shortcut Query** (p95 latency: <50ms):
```kotlin
@Query("""
    SELECT * FROM food_usage_stats
    ORDER BY usage_count DESC, food_name ASC
    LIMIT 6
""")
fun getTopUsedFoods(): Flow<List<FoodUsageStats>>
```

**Category Detail Query** (p95 latency: <200ms for 500+ foods):
```kotlin
@Query("""
    SELECT * FROM common_foods
    WHERE category = :category
    ORDER BY usage_count DESC, name ASC
""")
fun getFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>>
```

**Search Query** (p95 latency: <1s for 500+ foods):
```kotlin
@Query("""
    SELECT * FROM common_foods
    WHERE name LIKE '%' || :query || '%'
       OR search_terms LIKE '%' || :query || '%'
    ORDER BY usage_count DESC, name ASC
    LIMIT 50
""")
fun searchFoods(query: String): Flow<List<CommonFood>>
```

### Index Strategy

**Critical Indices**:
- `food_usage_stats.usage_count` (DESC) + `food_name` (ASC) → Quick-add query
- `common_foods.category` → Category detail query
- `food_items.timestamp` → Timeline queries

**Composite Index Consideration**:
- SQLite automatically creates composite index for multi-column ORDER BY
- No manual composite index needed for `(usage_count, food_name)`

---

## State Management

### ViewModel State

**FoodViewModel State**:
```kotlin
data class FoodUiState(
    val topUsedFoods: List<FoodUsageStats> = emptyList(),
    val selectedCategory: FoodCategory? = null,
    val categoryFoods: List<CommonFood> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<CommonFood> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**State Transitions**:
1. **Initial State**: `isLoading = false`, all lists empty
2. **Loading**: `isLoading = true` when fetching data
3. **Success**: `isLoading = false`, lists populated
4. **Error**: `isLoading = false`, `error` set with message
5. **Category Selection**: `selectedCategory` set, `categoryFoods` updated
6. **Search**: `searchQuery` updated, `searchResults` populated

---

## Success Metrics

**Database Performance** (from spec.md SC-001):
- Quick-add update latency: p95 < 200ms (database write to UI re-render)
- Food search latency: p95 < 1s for 500+ foods
- Migration duration: < 30s for 5000 historical entries
- App memory usage: < 200MB during typical usage (500+ foods)

**Data Integrity** (from spec.md SC-003):
- Migration success rate: 100% (0% data loss)
- Rollback success rate: 100% if migration fails
- FODMAP constraint validation: 100% (all foods have exactly one FODMAP level)

**Usage Tracking Accuracy** (from spec.md SC-008):
- Quick-add shortcuts update within 200ms of food logging
- Usage counts match actual logged entries (verified in tests)
- Sorting order: usage_count DESC, then alphabetically ASC (verified in tests)

---

## Next Steps

After reviewing this data model:

1. **Review & Approve**: Validate entity definitions match feature requirements
2. **Generate Contracts**: Create DAO interfaces in `/contracts/` directory
3. **Create Quickstart**: Generate developer onboarding guide
4. **Update Agent Context**: Add new technologies to Claude context file
5. **Phase 2: Implementation**: Begin coding data layer based on this model
