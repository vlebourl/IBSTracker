# Phase 0: Research Findings - Smart Food Categorization

**Date**: 2025-10-21
**Feature**: Smart Food Categorization System
**Branch**: `001-smart-food-categorization`

This document consolidates research findings for implementing the food categorization feature, covering Room database migrations, Material Design 3 UI patterns, StateFlow reactive state management, and EncryptedSharedPreferences security.

---

## 1. Room Database Migration Strategy (v8 → v9)

### Decision: Manual Migration with Transaction Support

**Rationale**:
- Complex transformations (category remapping, pre-population) exceed AutoMigration capabilities
- Room automatically wraps migrations in transactions for atomicity
- Manual migrations provide full control over data transformation and error handling
- All-or-nothing guarantee: either migration succeeds completely or database remains at v8

**Implementation Pattern**:
```kotlin
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Step 1: Create new tables
        database.execSQL("""
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
            )
        """)

        // Step 2: Add new columns with defaults
        database.execSQL("ALTER TABLE food_items ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'")
        database.execSQL("ALTER TABLE food_items ADD COLUMN ibs_impacts TEXT NOT NULL DEFAULT '[]'")

        // Step 3: Migrate existing data with category mapping
        database.execSQL("""
            UPDATE food_items
            SET category = CASE old_category
                WHEN 'DAIRY' THEN 'DAIRY'
                WHEN 'GLUTEN' THEN 'GRAINS'
                WHEN 'HIGH_FODMAP' THEN 'OTHER'
                WHEN 'SPICY' THEN 'PREPARED_FOODS'
                WHEN 'PROCESSED_FATTY' THEN 'PREPARED_FOODS'
                WHEN 'BEVERAGES' THEN 'BEVERAGES'
                WHEN 'FRUITS' THEN 'FRUITS'
                WHEN 'VEGETABLES' THEN 'VEGETABLES'
                ELSE 'OTHER'
            END
        """)

        // Step 4: Pre-populate common foods using prepared statement
        val stmt = database.compileStatement(
            "INSERT INTO common_foods (name, category, ibs_impacts, search_terms, created_at) VALUES (?, ?, ?, ?, ?)"
        )
        COMMON_FOODS.forEach { food ->
            stmt.clearBindings()
            stmt.bindString(1, food.name)
            stmt.bindString(2, food.category)
            stmt.bindString(3, food.ibsImpacts)
            stmt.bindString(4, food.searchTerms)
            stmt.bindLong(5, System.currentTimeMillis())
            stmt.executeInsert()
        }
    }
}
```

**Rollback Strategy**:
- Room's automatic transaction management ensures atomic rollback on failure
- Failed migration preserves database at v8 with all data intact
- User restarts app → migration attempted again
- After 3 failures (FR-006): offer JSON export option

**Testing Strategy**:
- Use `MigrationTestHelper` with multiple dataset sizes
- Test empty DB, 10 entries, 100 entries, 1000 entries, 5000 entries
- Verify schema changes AND data integrity after migration
- Test category remapping for all enum values
- Validate pre-population of 150 common foods

**Performance Expectations**:
- Empty DB: <500ms
- 10 entries: <1s
- 100 entries: <2s
- 1000 entries: <5s (within FR requirement)
- 5000 entries: <10s

**References**:
- [Official: Migrate your Room database](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Medium: Understanding migrations with Room](https://medium.com/androiddevelopers/understanding-migrations-with-room-f01e04b07929)
- [Medium: Testing Room migrations](https://medium.com/androiddevelopers/testing-room-migrations-be93cdb0d975)

---

## 2. Material Design 3 Implementation

### Decision: M3 Component-Based Approach with Adaptive Layouts

**Rationale**:
- ModalBottomSheet (M3 1.1.0+) provides native bottom sheet with built-in accessibility
- LazyVerticalGrid with GridCells.Fixed(3) ensures consistent 3-column layout
- Tertiary color system supports 12 categorical colors with guaranteed WCAG AA contrast
- WindowSizeClass enables responsive design for phones to tablets
- Compose BOM 2023.08.00+ includes all necessary M3 components

**Component Patterns**:

1. **Category Grid Layout**:
```kotlin
LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    items(categories, key = { it.id }) { category ->
        CategoryCard(
            category = category,
            onClick = { onCategoryClick(category) },
            modifier = Modifier.animateItem() // Built-in reorder animation
        )
    }
}
```

2. **Category Card with Touch Targets**:
```kotlin
Card(
    onClick = onClick,
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    ),
    modifier = modifier
        .minimumInteractiveComponentSize() // Ensures 48dp minimum
        .semantics {
            contentDescription = "${category.name} category"
            role = Role.Button
        }
) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
```

3. **Bottom Sheet Dialogs**:
```kotlin
ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState()
) {
    AddFoodDialogContent(
        onSave = onSave,
        onCancel = onDismiss
    )
}
```

**Accessibility Compliance**:
- **WCAG AA Contrast**: Material3 color pairs (tertiaryContainer + onTertiaryContainer) guarantee 4.5:1 contrast
- **Touch Targets**: minimumInteractiveComponentSize() enforces 48dp × 48dp minimum
- **Screen Reader Support**: Semantic properties provide meaningful TalkBack announcements
- **Ripple Effects**: Automatic from Material3 Card(onClick = ...) with proper visual feedback

**Icon Selection** (Material Icons Extended):
```kotlin
enum class FoodCategory(val icon: ImageVector) {
    GRAINS(Icons.Outlined.Grain),
    PROTEINS(Icons.Outlined.Restaurant),
    DAIRY(Icons.Outlined.LocalDrink),
    FRUITS(Icons.Outlined.Apple),
    VEGETABLES(Icons.Outlined.Eco),
    LEGUMES(Icons.Outlined.Widgets),
    NUTS_SEEDS(Icons.Outlined.Spa),
    BEVERAGES(Icons.Outlined.LocalCafe),
    SWEETS(Icons.Outlined.Cake),
    FATS_OILS(Icons.Outlined.Opacity),
    PREPARED_FOODS(Icons.Outlined.Fastfood),
    OTHER(Icons.Outlined.MoreHoriz)
}
```

**References**:
- [Material Design 3 in Compose](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Bottom Sheets](https://developer.android.com/develop/ui/compose/components/bottom-sheets)
- [Lists and Grids](https://developer.android.com/develop/ui/compose/lists)
- [Accessibility in Compose](https://developer.android.com/codelabs/jetpack-compose-accessibility)

---

## 3. StateFlow Reactive State Management

### Decision: StateFlow with Single Source of Truth Pattern

**Rationale**:
- StateFlow always has a value, ideal for UI state that survives configuration changes
- `collectAsState()` in Compose provides seamless integration with recomposition
- SharingStarted.WhileSubscribed(5000) keeps Flow active during config changes without re-querying
- Room database is the single source of truth; StateFlow provides reactive caching
- Existing FoodViewModel already implements this pattern correctly

**ViewModel Pattern**:
```kotlin
val commonFoods: StateFlow<List<CommonFood>> = dataRepository.getCommonFoods()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

val quickAddItems: StateFlow<List<CommonFood>> = dataRepository.getTopFoodsByUsage(limit = 6)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

**Usage-Based Sorting** (Room DAO):
```kotlin
@Query("""
    SELECT * FROM common_foods
    ORDER BY usage_count DESC, name ASC
    LIMIT :limit
""")
fun getTopFoodsByUsage(limit: Int): Flow<List<CommonFood>>
```

**Search with Debouncing**:
```kotlin
val searchResults: StateFlow<List<CommonFood>> = _searchQuery
    .debounce(300)  // Wait 300ms after user stops typing
    .filter { it.length >= 2 }
    .distinctUntilChanged()
    .mapLatest { query ->
        dataRepository.searchFoods(query)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
```

**Performance Optimization**:
- **Prefer SQL JOINs** over Flow combine() operators for data aggregation
- Use `derivedStateOf` in Composables for computed values
- Collect StateFlow at specific composable level, not top-level screen
- Mark data classes with @Immutable/@Stable to enable smart recomposition

**Quick-Add Update Target** (<200ms per FR-048):
- Room Flow automatically updates when data changes
- 5-second WhileSubscribed timeout eliminates re-query delays
- StateFlow caching provides instant UI updates

**References**:
- [StateFlow and SharedFlow | Android Developers](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Kotlin flows on Android](https://developer.android.com/kotlin/flow)
- [UI State production](https://developer.android.com/topic/architecture/ui-layer/state-production)

---

## 4. EncryptedSharedPreferences Security

### Decision: Continue Current Implementation with Backup Exclusion

**Rationale**:
- Existing SessionManager and SettingsRepository already implement EncryptedSharedPreferences correctly
- AES256_GCM master key + AES256_SIV keys + AES256_GCM values provides AEAD security
- Android Keystore hardware-backed encryption available on API 24+
- Library deprecated but functional for existing projects with manual Tink updates
- Only missing piece: backup exclusion rules

**Encryption Scheme**:
```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secret_shared_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

**Failure Handling** (already implemented correctly):
```kotlin
private fun createEncryptedPrefs(): SharedPreferences {
    return try {
        EncryptedSharedPreferences.create(...)
    } catch (e: Exception) {
        try {
            context.deleteSharedPreferences(ENCRYPTED_PREFS_NAME)
            EncryptedSharedPreferences.create(...)
        } catch (e2: Exception) {
            // Fallback to regular SharedPreferences
            context.getSharedPreferences("${ENCRYPTED_PREFS_NAME}_fallback", Context.MODE_PRIVATE)
        }
    }
}
```

**CRITICAL: Backup Exclusion Required**

Add to `app/src/main/res/xml/backup_rules.xml`:
```xml
<full-backup-content>
    <exclude domain="sharedpref" path="auth_session"/>
    <exclude domain="sharedpref" path="encrypted_settings"/>
</full-backup-content>
```

Add to `app/src/main/res/xml/data_extraction_rules.xml`:
```xml
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="sharedpref" path="auth_session"/>
        <exclude domain="sharedpref" path="encrypted_settings"/>
    </cloud-backup>
    <device-transfer>
        <exclude domain="sharedpref" path="auth_session"/>
        <exclude domain="sharedpref" path="encrypted_settings"/>
    </device-transfer>
</data-extraction-rules>
```

**Data Classification**:
- ✅ ENCRYPT: User email, backup password (already implemented)
- ⚠️ CONSIDER: Room database encryption with SQLCipher (if HIPAA/GDPR required)
- ❌ DON'T ENCRYPT: Language preference, UI settings (use DataStore - already implemented)

**Performance Impact**:
- Minimal overhead for 2 values (email + password)
- Hardware AES acceleration on modern devices (7-30x faster)
- No impact on bulk data (Room database handles food/symptom records)

**References**:
- [EncryptedSharedPreferences API](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)
- [Android Keystore System](https://developer.android.com/training/articles/keystore)
- [Data Encryption on Android with Jetpack Security](https://medium.com/androiddevelopers/data-encryption-on-android-with-jetpack-security-e4cb0b2d2a9)

---

## 5. JSON Export/Import for Migration Failures

### Decision: Kotlinx Serialization with Room Type Converters

**Rationale** (from FR-049):
- JSON format is human-readable for user verification
- Structured format preserves complex data (categories, IBS impacts lists)
- Kotlinx Serialization integrates well with Kotlin data classes
- Re-importable after migration fix or fresh v9 installation

**Implementation Pattern**:
```kotlin
@Serializable
data class FoodExport(
    val version: Int,
    val exportDate: Long,
    val foods: List<FoodItemExport>,
    val commonFoods: List<CommonFoodExport>,
    val usageStats: List<FoodUsageStatsExport>
)

object JsonExportImport {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportDatabase(context: Context, repository: DataRepository): File {
        val export = FoodExport(
            version = 9,
            exportDate = System.currentTimeMillis(),
            foods = repository.getAllFoodItems().first(),
            commonFoods = repository.getAllCommonFoods().first(),
            usageStats = repository.getAllUsageStats().first()
        )

        val jsonString = json.encodeToString(export)
        val file = File(context.getExternalFilesDir(null), "ibs_tracker_export_${export.exportDate}.json")
        file.writeText(jsonString)
        return file
    }

    suspend fun importDatabase(file: File, repository: DataRepository) {
        val jsonString = file.readText()
        val export = json.decodeFromString<FoodExport>(jsonString)

        repository.importData(
            foods = export.foods,
            commonFoods = export.commonFoods,
            usageStats = export.usageStats
        )
    }
}
```

**User Flow** (from FR-006 clarification):
1. Migration fails → Automatic rollback to v8
2. Show error dialog with "Retry Migration" button
3. After 3 failed attempts → Show "Export Data" button
4. User exports to JSON → Can manually send to support or reinstall app
5. Fresh v9 installation → Import JSON to restore data

**Success Criteria** (SC-014):
- 100% data preservation in export/import roundtrip
- All FoodItems, categories, IBS impacts, usage counts, timestamps preserved
- Zero data loss verified via automated tests comparing pre-export and post-import states

---

## 6. Key Implementation Decisions Summary

| Decision Area | Choice | Rationale | Success Metric |
|---------------|--------|-----------|----------------|
| **Migration Strategy** | Manual Migration with transactions | Complex transformations, full control | 0% data loss (SC-003) |
| **Rollback Approach** | Automatic + Retry (max 3) + JSON export | Data preservation first, user recovery option | Graceful failure handling |
| **UI Framework** | Material Design 3 (ModalBottomSheet, LazyVerticalGrid) | Native M3 components, built-in accessibility | WCAG AA (SC-007) |
| **Color System** | Tertiary color roles for 12 categories | Guaranteed contrast, semantic categories | 4.5:1 contrast (FR-044) |
| **State Management** | StateFlow with WhileSubscribed(5000) | Config-change resilient, reactive caching | <200ms updates (SC-013) |
| **Sorting Logic** | SQL ORDER BY usage_count DESC, name ASC | Database-level performance, consistent | 100% accuracy (SC-008) |
| **Search Pattern** | Flow debounce(300ms) + mapLatest | Reactive, efficient, minimal queries | <1s p95 (SC-004) |
| **Data Security** | EncryptedSharedPreferences for credentials | Hardware-backed encryption, standards compliance | Encryption verified (SC-015) |
| **Backup Strategy** | Exclude encrypted prefs, use Google Drive | Prevent key loss, user-controlled backups | No restore failures |
| **Export Format** | JSON with Kotlinx Serialization | Human-readable, re-importable, structured | 100% roundtrip (SC-014) |

---

## 7. Architecture Alignment

All research findings align with IBS Tracker's existing architecture:

✅ **Clean Architecture**:
- Data layer: Room migrations, DAOs (no UI dependencies)
- Domain layer: StateFlow in ViewModels (no platform code)
- UI layer: Jetpack Compose with Material3 (stateless, data-driven)

✅ **Material Design 3**:
- Tertiary color system for categories
- 48dp touch targets, WCAG AA contrast
- Bottom sheet dialogs, ripple effects

✅ **Database Integrity**:
- Explicit migration v8→v9 with SQL
- Transaction-based rollback safety
- Comprehensive testing strategy

✅ **Existing Patterns**:
- Manual DI via AppContainer (no changes)
- StateFlow with collectAsState() (FoodViewModel pattern)
- EncryptedSharedPreferences (SessionManager pattern)
- DataStore for preferences (SettingsRepository pattern)

---

## Next Steps

With research complete, proceed to **Phase 1: Design & Contracts**:

1. Generate `data-model.md` with complete entity definitions
2. Create `/contracts/` with DAO interfaces
3. Generate `quickstart.md` for developer onboarding
4. Update agent context with new technologies/patterns

**References Complete**: All findings documented with official Android documentation links, Medium articles from Android Developers, and community best practices.
