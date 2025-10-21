# Database Schema - Smart Food Categorization

## 📊 Schema Overview

### New Enums

#### FoodCategory (12 categories)
```kotlin
enum class FoodCategory(
    val color: Color,
    val icon: ImageVector
) {
    GRAINS(Color(0xFFDEB887), Icons.Default.Grain),           // Burlywood
    PROTEINS(Color(0xFFE57373), Icons.Default.Restaurant),    // Light Red
    DAIRY(Color(0xFF81D4FA), Icons.Default.Coffee),           // Light Blue
    FRUITS(Color(0xFFAED581), Icons.Default.Apple),           // Light Green
    VEGETABLES(Color(0xFF66BB6A), Icons.Default.Grass),       // Green
    LEGUMES(Color(0xFFBA68C8), Icons.Default.Widgets),        // Purple
    NUTS_SEEDS(Color(0xFFFFB74D), Icons.Default.Spa),         // Orange
    BEVERAGES(Color(0xFF4FC3F7), Icons.Default.LocalCafe),    // Cyan
    SWEETS(Color(0xFFF48FB1), Icons.Default.Cake),            // Pink
    FATS_OILS(Color(0xFFFFF176), Icons.Default.Opacity),      // Yellow
    PREPARED_FOODS(Color(0xFFFFAB91), Icons.Default.Fastfood),// Deep Orange
    OTHER(Color(0xFF90A4AE), Icons.Default.MoreHoriz)         // Blue Grey
}
```

**Color Rationale:**
- Material Design 3 color palette (300-400 range for accessibility)
- Semantic colors (green for vegetables, blue for dairy, etc.)
- Distinct colors for 3-column grid visibility
- WCAG AA compliant contrast ratios

#### IBSImpact (11 attributes)
```kotlin
enum class IBSImpact(
    val displayName: String,
    val description: String,
    val category: AttributeCategory
) {
    // FODMAP Level (mutually exclusive)
    HIGH_FODMAP(
        "High FODMAP",
        "Foods high in fermentable carbs that can cause bloating and gas",
        AttributeCategory.FODMAP
    ),
    MODERATE_FODMAP(
        "Moderate FODMAP",
        "Medium FODMAP content, may trigger symptoms in some people",
        AttributeCategory.FODMAP
    ),
    LOW_FODMAP(
        "Low FODMAP",
        "Low in fermentable carbs, generally well tolerated",
        AttributeCategory.FODMAP
    ),

    // Composition (can combine)
    GLUTEN_CONTAINING(
        "Contains Gluten",
        "Contains wheat, barley, or rye protein",
        AttributeCategory.COMPOSITION
    ),
    LACTOSE_CONTAINING(
        "Contains Lactose",
        "Contains milk sugar (lactose)",
        AttributeCategory.COMPOSITION
    ),

    // Content Type (can combine)
    HIGH_FAT(
        "High Fat",
        "High fat content, may trigger IBS-D (diarrhea)",
        AttributeCategory.CONTENT
    ),
    HIGH_FIBER(
        "High Fiber",
        "High fiber content, can help or trigger depending on type",
        AttributeCategory.CONTENT
    ),
    SPICY(
        "Spicy",
        "Contains hot spices that may irritate digestive system",
        AttributeCategory.CONTENT
    ),
    ARTIFICIAL_SWEETENER(
        "Artificial Sweetener",
        "Contains sorbitol, xylitol, or other sugar alcohols",
        AttributeCategory.CONTENT
    ),

    // Beverage Specific (only show for BEVERAGES category)
    CAFFEINATED(
        "Caffeinated",
        "Contains caffeine which stimulates bowel movement",
        AttributeCategory.BEVERAGE
    ),
    CARBONATED(
        "Carbonated",
        "Fizzy drinks that can cause gas and bloating",
        AttributeCategory.BEVERAGE
    ),
    ALCOHOLIC(
        "Alcoholic",
        "Contains alcohol which can trigger IBS symptoms",
        AttributeCategory.BEVERAGE
    ),
    ACIDIC(
        "Acidic",
        "High acidity (citrus, vinegar) may irritate stomach",
        AttributeCategory.BEVERAGE
    )
}

enum class AttributeCategory {
    FODMAP,      // Radio buttons (mutually exclusive)
    COMPOSITION, // Checkboxes
    CONTENT,     // Checkboxes
    BEVERAGE     // Checkboxes (only for BEVERAGES category)
}
```

### Updated Tables

#### FoodItem (Modified)
```kotlin
@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val quantity: String,
    val date: Date,
    val notes: String = "",

    // NEW FIELDS
    @ColumnInfo(name = "category")
    val category: FoodCategory = FoodCategory.OTHER,

    @ColumnInfo(name = "ibs_impacts")
    val ibsImpacts: List<IBSImpact> = listOf(IBSImpact.LOW_FODMAP),

    // Track if this is a user-added custom food vs. common food
    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = true,

    // Reference to CommonFood if this came from database
    @ColumnInfo(name = "common_food_id")
    val commonFoodId: Long? = null
)
```

#### CommonFood (New Table)
Pre-populated database of common foods with categorization.

```kotlin
@Entity(tableName = "common_foods")
data class CommonFood(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val category: FoodCategory,
    val ibsImpacts: List<IBSImpact>,

    // Search optimization
    val searchTerms: List<String>, // ["coffee", "espresso", "latte", "cappuccino"]

    // Usage tracking (for quick-add sorting)
    @ColumnInfo(name = "usage_count")
    val usageCount: Int = 0,

    // Localization support
    @ColumnInfo(name = "name_fr")
    val nameFr: String? = null,

    @ColumnInfo(name = "name_en")
    val nameEn: String? = null,

    @ColumnInfo(name = "is_verified")
    val isVerified: Boolean = true, // True for pre-populated, false for user-added

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

#### FoodUsageStats (New Table)
Track food usage for smart quick-add sorting.

```kotlin
@Entity(tableName = "food_usage_stats")
data class FoodUsageStats(
    @PrimaryKey
    val foodName: String,

    val category: FoodCategory,
    val totalUses: Int,
    val lastUsed: Long,
    val commonFoodId: Long? = null
)
```

## 🔄 Migration Strategy

### Migration Path: v8 → v9

```kotlin
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Add new columns to food_items
        database.execSQL("""
            ALTER TABLE food_items
            ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'
        """)

        database.execSQL("""
            ALTER TABLE food_items
            ADD COLUMN ibs_impacts TEXT NOT NULL DEFAULT '["LOW_FODMAP"]'
        """)

        database.execSQL("""
            ALTER TABLE food_items
            ADD COLUMN is_custom INTEGER NOT NULL DEFAULT 1
        """)

        database.execSQL("""
            ALTER TABLE food_items
            ADD COLUMN common_food_id INTEGER
        """)

        // Step 2: Create new tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS common_foods (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                ibs_impacts TEXT NOT NULL,
                search_terms TEXT NOT NULL,
                usage_count INTEGER NOT NULL DEFAULT 0,
                name_fr TEXT,
                name_en TEXT,
                is_verified INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS food_usage_stats (
                foodName TEXT PRIMARY KEY NOT NULL,
                category TEXT NOT NULL,
                totalUses INTEGER NOT NULL DEFAULT 0,
                lastUsed INTEGER NOT NULL,
                common_food_id INTEGER
            )
        """)

        // Step 3: Migrate existing food items (OLD → NEW category mapping)
        migrateFoodCategories(database)

        // Step 4: Pre-populate common foods
        prePopulateCommonFoods(database)

        // Step 5: Build usage stats from existing food_items
        buildUsageStats(database)
    }
}
```

### Category Migration Mapping

**OLD → NEW Category Mapping:**

```kotlin
private fun migrateFoodCategories(database: SupportSQLiteDatabase) {
    val migrations = mapOf(
        "DAIRY" to "DAIRY",
        "GLUTEN" to "GRAINS",  // Most gluten = grains
        "HIGH_FODMAP" to "OTHER",  // Need manual review
        "SPICY" to "PREPARED_FOODS",  // Often prepared/sauced
        "PROCESSED_FATTY" to "PREPARED_FOODS",
        "BEVERAGES" to "BEVERAGES",
        "FRUITS" to "FRUITS",
        "VEGETABLES" to "VEGETABLES",
        "OTHER" to "OTHER"
    )

    // Migrate categories
    migrations.forEach { (old, new) ->
        database.execSQL("""
            UPDATE food_items
            SET category = '$new'
            WHERE category = '$old'
        """)
    }

    // Assign IBS impacts based on old category
    assignIBSImpactsByOldCategory(database)
}

private fun assignIBSImpactsByOldCategory(database: SupportSQLiteDatabase) {
    // DAIRY → LACTOSE_CONTAINING
    database.execSQL("""
        UPDATE food_items
        SET ibs_impacts = '["LACTOSE_CONTAINING", "LOW_FODMAP"]'
        WHERE category = 'DAIRY'
    """)

    // GRAINS (from GLUTEN) → GLUTEN_CONTAINING, HIGH_FODMAP
    database.execSQL("""
        UPDATE food_items
        SET ibs_impacts = '["GLUTEN_CONTAINING", "HIGH_FODMAP"]'
        WHERE category = 'GRAINS'
    """)

    // PREPARED_FOODS (from SPICY/PROCESSED_FATTY) → HIGH_FAT, SPICY
    database.execSQL("""
        UPDATE food_items
        SET ibs_impacts = '["HIGH_FAT", "SPICY", "MODERATE_FODMAP"]'
        WHERE category = 'PREPARED_FOODS'
    """)

    // Default everything else to LOW_FODMAP
    database.execSQL("""
        UPDATE food_items
        SET ibs_impacts = '["LOW_FODMAP"]'
        WHERE ibs_impacts = '["LOW_FODMAP"]'
    """)
}
```

## 📝 Type Converters

```kotlin
class Converters {
    // Existing Date converter
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // NEW: IBSImpact List converter
    @TypeConverter
    fun fromIBSImpactList(value: List<IBSImpact>?): String {
        return value?.joinToString(",") { it.name } ?: ""
    }

    @TypeConverter
    fun toIBSImpactList(value: String): List<IBSImpact> {
        return if (value.isEmpty()) {
            listOf(IBSImpact.LOW_FODMAP)
        } else {
            value.split(",").mapNotNull {
                try {
                    IBSImpact.valueOf(it)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    // NEW: FoodCategory converter
    @TypeConverter
    fun fromFoodCategory(value: FoodCategory): String {
        return value.name
    }

    @TypeConverter
    fun toFoodCategory(value: String): FoodCategory {
        return try {
            FoodCategory.valueOf(value)
        } catch (e: Exception) {
            FoodCategory.OTHER
        }
    }

    // NEW: String List converter (for search terms)
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}
```

## 🗃️ DAOs

### CommonFoodDao
```kotlin
@Dao
interface CommonFoodDao {
    @Query("SELECT * FROM common_foods ORDER BY usage_count DESC")
    fun getAllCommonFoods(): Flow<List<CommonFood>>

    @Query("SELECT * FROM common_foods WHERE category = :category ORDER BY usage_count DESC, name ASC")
    fun getFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>>

    @Query("""
        SELECT * FROM common_foods
        WHERE name LIKE '%' || :query || '%'
        OR search_terms LIKE '%' || :query || '%'
        ORDER BY usage_count DESC
        LIMIT 20
    """)
    fun searchFoods(query: String): Flow<List<CommonFood>>

    @Query("UPDATE common_foods SET usage_count = usage_count + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommonFood(food: CommonFood): Long

    @Update
    suspend fun updateCommonFood(food: CommonFood)

    @Delete
    suspend fun deleteCommonFood(food: CommonFood)
}
```

### FoodUsageStatsDao
```kotlin
@Dao
interface FoodUsageStatsDao {
    @Query("SELECT * FROM food_usage_stats ORDER BY totalUses DESC, lastUsed DESC")
    fun getAllStats(): Flow<List<FoodUsageStats>>

    @Query("""
        SELECT * FROM food_usage_stats
        WHERE category = :category
        ORDER BY totalUses DESC, foodName ASC
        LIMIT 6
    """)
    fun getTopFoodsByCategory(category: FoodCategory): Flow<List<FoodUsageStats>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: FoodUsageStats)

    suspend fun recordFoodUse(foodName: String, category: FoodCategory, commonFoodId: Long? = null) {
        // Implementation in repository
    }
}
```

## 🎯 Repository Updates

```kotlin
class DataRepository(
    private val foodItemDao: FoodItemDao,
    private val commonFoodDao: CommonFoodDao,
    private val foodUsageStatsDao: FoodUsageStatsDao,
    // ... other DAOs
) {
    // Existing methods...

    // NEW: Common food methods
    fun getAllCommonFoods(): Flow<List<CommonFood>> = commonFoodDao.getAllCommonFoods()

    fun getFoodsByCategory(category: FoodCategory): Flow<List<CommonFood>> =
        commonFoodDao.getFoodsByCategory(category)

    fun searchFoods(query: String): Flow<List<CommonFood>> =
        commonFoodDao.searchFoods(query)

    suspend fun addCustomFood(name: String, category: FoodCategory, impacts: List<IBSImpact>): Long {
        val commonFood = CommonFood(
            name = name,
            category = category,
            ibsImpacts = impacts,
            searchTerms = listOf(name.lowercase()),
            isVerified = false
        )
        return commonFoodDao.insertCommonFood(commonFood)
    }

    // NEW: Usage tracking
    suspend fun recordFoodUse(foodName: String, category: FoodCategory, commonFoodId: Long?) {
        // Update usage stats
        val currentStats = foodUsageStatsDao.getStatsByName(foodName)
        val newStats = if (currentStats != null) {
            currentStats.copy(
                totalUses = currentStats.totalUses + 1,
                lastUsed = System.currentTimeMillis()
            )
        } else {
            FoodUsageStats(
                foodName = foodName,
                category = category,
                totalUses = 1,
                lastUsed = System.currentTimeMillis(),
                commonFoodId = commonFoodId
            )
        }
        foodUsageStatsDao.insertOrUpdate(newStats)

        // Also increment common food usage if applicable
        commonFoodId?.let { commonFoodDao.incrementUsageCount(it) }
    }
}
```

---

**Next Steps:**
1. Implement new models and enums
2. Create migration class
3. Update database version
4. Test migration with sample data
