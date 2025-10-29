# Quickstart: Fix Custom Food Addition Bug

**Feature**: 004-fix-custom-food-persistence
**Estimated Time**: 2-3 hours
**Complexity**: Low (single file modification + tests)

---

## What You'll Build

Fix a critical bug where custom foods added by users don't appear in category lists or search results. The fix involves ~15 lines of code in `DataRepository.kt`.

**Before**: Custom food "Soja" → Saved as FoodItem → Not visible in UI (missing CommonFood entry)
**After**: Custom food "Soja" → Creates CommonFood → Links FoodItem → Visible everywhere

---

## Prerequisites

- [x] Android Studio installed (Arctic Fox or later)
- [x] Kotlin 1.8.20
- [x] Project already uses Room 2.6.1
- [x] Database schema v9 (common_foods table exists)
- [x] Familiarity with Kotlin coroutines and Flow

---

## Quick Start (5 minutes)

### 1. Verify Current Bug

```bash
# Run app on emulator/device
./gradlew installDebug

# Manual test:
# 1. Open Food screen
# 2. Select "Other" category
# 3. Click "Enter custom" → Type "Soja" → Save
# 4. Navigate back to "Other" category
# ❌ BUG: "Soja" does not appear in food list
```

### 2. Understand the Problem

```kotlin
// Current code (DataRepository.kt:51-70)
suspend fun insertFoodItem(foodItem: FoodItem): Long {
    val rowId = foodItemDao.insert(foodItem)

    // ❌ BUG: Never creates CommonFood for custom foods
    // ❌ BUG: commonFoodId is always null

    foodItem.commonFoodId?.let { commonFoodId ->
        commonFoodDao.incrementUsageCountById(commonFoodId)
    }

    upsertUsageStats(...)
    return rowId
}
```

**Why UI doesn't show custom foods**:
- `FoodScreen.kt` displays foods from `commonFoodDao.getCommonFoodsByCategory()`
- Custom foods only exist in `food_items` table (not `common_foods`)
- Search uses `commonFoodDao.searchCommonFoods()` (misses custom foods)

### 3. Apply the Fix

Open `app/src/main/java/com/tiarkaerell/ibstracker/data/repository/DataRepository.kt`:

```kotlin
suspend fun insertFoodItem(foodItem: FoodItem): Long {
    // ✅ NEW: Step 1 - Check for existing CommonFood
    val existingCommonFood = getCommonFoodByName(foodItem.name).first()

    // ✅ NEW: Step 2 - Create CommonFood if not found
    val commonFood = existingCommonFood ?: run {
        val newCommonFood = CommonFood(
            name = foodItem.name,
            category = foodItem.category,
            ibsImpacts = listOf(IBSImpact.FODMAP_LOW),  // Safe default
            isVerified = false,  // Marks as custom food
            usageCount = 0,
            searchTerms = "",    // Empty for custom
            nameEn = "",
            nameFr = ""
        )
        val commonFoodId = commonFoodDao.insert(newCommonFood)
        newCommonFood.copy(id = commonFoodId)
    }

    // ✅ MODIFIED: Step 3 - Link FoodItem to CommonFood
    val updatedFoodItem = foodItem.copy(commonFoodId = commonFood.id)
    val rowId = foodItemDao.insert(updatedFoodItem)

    // ✅ UPDATED: Step 4 - Increment usage count (now always runs)
    commonFoodDao.incrementUsageCountById(commonFood.id)

    // ✅ UNCHANGED: Step 5 - Update usage stats
    upsertUsageStats(
        foodItem.name,
        foodItem.category,
        foodItem.timestamp,
        foodItem.ibsImpacts,
        true  // isFromCommonFoods = true
    )

    return rowId
}
```

### 4. Verify the Fix

```bash
# Rebuild and run
./gradlew installDebug

# Manual test:
# 1. Add custom food "Soja" to "Other" category
# 2. Navigate back to "Other" category
# ✅ FIXED: "Soja" appears in food list
# 3. Use search bar to search "Soja"
# ✅ FIXED: "Soja" appears in search results
# 4. Add "Soja" again (duplicate)
# ✅ EXPECTED: No duplicate created, usage count increments
```

---

## Development Workflow

### Step 1: Write Tests (15 minutes)

Create `app/src/androidTest/java/com/tiarkaerell/ibstracker/data/repository/CustomFoodPersistenceTest.kt`:

```kotlin
@RunWith(AndroidJUnit4::class)
class CustomFoodPersistenceTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: DataRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = DataRepository(
            database.foodItemDao(),
            database.commonFoodDao(),
            database.foodUsageStatsDao(),
            database.symptomDao()
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testCustomFoodCreatesCommonFood() = runBlocking {
        // Arrange
        val customFood = FoodItem(
            name = "Soja",
            category = FoodCategory.OTHER,
            timestamp = Date()
        )

        // Act
        repository.insertFoodItem(customFood)

        // Assert
        val commonFood = repository.getCommonFoodByName("Soja").first()
        assertNotNull(commonFood)
        assertEquals("Soja", commonFood?.name)
        assertEquals(FoodCategory.OTHER, commonFood?.category)
        assertEquals(false, commonFood?.isVerified)
        assertEquals(1, commonFood?.usageCount)
    }

    @Test
    fun testDuplicateCustomFoodReusesCommonFood() = runBlocking {
        // Arrange
        val food1 = FoodItem(name = "Soja", category = FoodCategory.OTHER)
        val food2 = FoodItem(name = "Soja", category = FoodCategory.OTHER)

        // Act
        repository.insertFoodItem(food1)
        repository.insertFoodItem(food2)

        // Assert
        val commonFoods = repository.getAllCommonFoods().first()
        val sojaFoods = commonFoods.filter { it.name == "Soja" }
        assertEquals(1, sojaFoods.size)  // Only one CommonFood created
        assertEquals(2, sojaFoods[0].usageCount)  // Usage incremented
    }

    @Test
    fun testCustomFoodAppearsInCategoryList() = runBlocking {
        // Arrange
        val customFood = FoodItem(name = "Soja", category = FoodCategory.OTHER)

        // Act
        repository.insertFoodItem(customFood)

        // Assert
        val categoryFoods = repository.getCommonFoodsByCategory(FoodCategory.OTHER).first()
        val sojaFood = categoryFoods.find { it.name == "Soja" }
        assertNotNull(sojaFood)
    }

    @Test
    fun testCustomFoodAppearsInSearch() = runBlocking {
        // Arrange
        val customFood = FoodItem(name = "Soja", category = FoodCategory.OTHER)

        // Act
        repository.insertFoodItem(customFood)

        // Assert
        val searchResults = repository.searchCommonFoods("Soja").first()
        val sojaFood = searchResults.find { it.name == "Soja" }
        assertNotNull(sojaFood)
    }
}
```

### Step 2: Run Tests (Expect Failures)

```bash
./gradlew connectedAndroidTest

# Expected: All 4 tests FAIL (bug not fixed yet)
```

### Step 3: Implement Fix (30 minutes)

Follow the code in **Quick Start Step 3** above.

### Step 4: Run Tests (Expect Success)

```bash
./gradlew connectedAndroidTest

# Expected: All 4 tests PASS ✅
```

### Step 5: Manual Testing (15 minutes)

**Test Case 1: Add New Custom Food**
1. Open Food screen
2. Select "Other" category
3. Click "Enter custom" → Type "Tofu" → Save
4. ✅ Verify: "Tofu" appears in "Other" category list
5. ✅ Verify: Search for "Tofu" returns result

**Test Case 2: Add Duplicate Custom Food**
1. Add "Tofu" again to "Other" category
2. ✅ Verify: No duplicate in category list
3. ✅ Verify: "Tofu" usage count increases (visual indicator if implemented)

**Test Case 3: Pre-populated Food (No Regression)**
1. Select "Grains & Starches" category
2. Click "Riz blanc" (pre-populated food)
3. ✅ Verify: Food logs correctly
4. ✅ Verify: No duplicate CommonFood created

**Test Case 4: Search Custom Food**
1. Use search bar → Type "Tofu"
2. ✅ Verify: "Tofu" appears in results
3. ✅ Verify: Can select and log from search results

**Test Case 5: Category Sorting**
1. Add custom food "Zucchini" to "Vegetables"
2. Log "Zucchini" 5 times
3. Log pre-populated "Carrot" 3 times
4. ✅ Verify: "Zucchini" appears before "Carrot" (usage-based sort)

### Step 6: Performance Testing (15 minutes)

**Test Large Dataset**:
```kotlin
// Add test method
@Test
fun testPerformanceWith200CustomFoods() = runBlocking {
    // Arrange: Create 200 custom foods
    repeat(200) { i ->
        val food = FoodItem(name = "CustomFood$i", category = FoodCategory.OTHER)
        repository.insertFoodItem(food)
    }

    // Act: Measure category load time
    val startTime = System.currentTimeMillis()
    val foods = repository.getCommonFoodsByCategory(FoodCategory.OTHER).first()
    val duration = System.currentTimeMillis() - startTime

    // Assert
    assertTrue(foods.size >= 200)
    assertTrue(duration < 500)  // p95 < 500ms
}
```

### Step 7: Code Review Checklist

- [ ] Fix compiles without errors
- [ ] All tests pass (4 new tests + existing tests)
- [ ] No database migration required
- [ ] Backward compatible (old FoodItems still work)
- [ ] No breaking changes to existing APIs
- [ ] Performance meets targets (< 500ms for 200 foods)
- [ ] Code follows existing patterns (repository, suspend functions, Flow)
- [ ] FODMAP validation enforced (exactly one FODMAP level)

---

## Common Issues

### Issue 1: "Existing CommonFood always null"

**Symptom**: Custom foods always create duplicates

**Cause**: `getCommonFoodByName()` returns Flow, not direct value

**Fix**: Use `.first()` to get value from Flow:
```kotlin
val existing = getCommonFoodByName(name).first()  // ✅ Correct
// NOT: val existing = getCommonFoodByName(name)   // ❌ Wrong type
```

---

### Issue 2: "FODMAP validation fails"

**Symptom**: `IllegalArgumentException: CommonFood must have exactly one FODMAP level`

**Cause**: Missing FODMAP level or multiple levels

**Fix**: Ensure exactly one FODMAP level:
```kotlin
ibsImpacts = listOf(IBSImpact.FODMAP_LOW)  // ✅ Exactly one
// NOT: ibsImpacts = emptyList()             // ❌ Zero levels
// NOT: ibsImpacts = listOf(FODMAP_LOW, FODMAP_HIGH)  // ❌ Two levels
```

---

### Issue 3: "UI doesn't update after adding food"

**Symptom**: Custom food doesn't appear immediately in category list

**Cause**: Flow not triggering recomposition

**Fix**: Verify `collectAsState()` in UI:
```kotlin
val commonFoods by foodViewModel
    .getCommonFoodsByCategory(category)
    .collectAsState(initial = emptyList())  // ✅ Auto-updates
```

---

### Issue 4: "Tests fail with 'Table not found'"

**Symptom**: Room tests crash with `SQLiteException`

**Cause**: Schema v9 not applied to in-memory database

**Fix**: Include all migrations in test setup:
```kotlin
database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
    .addMigrations(MIGRATION_8_9)  // Add if testing migrations
    .build()
```

---

## File Checklist

**Files to Modify**:
- [x] `app/src/main/java/com/tiarkaerell/ibstracker/data/repository/DataRepository.kt`

**Files to Create**:
- [x] `app/src/androidTest/java/.../CustomFoodPersistenceTest.kt`

**Files to Read** (for context):
- [ ] `app/src/main/java/.../dao/CommonFoodDao.kt`
- [ ] `app/src/main/java/.../dao/FoodItemDao.kt`
- [ ] `app/src/main/java/.../model/CommonFood.kt`
- [ ] `app/src/main/java/.../model/FoodItem.kt`
- [ ] `app/src/main/java/.../ui/viewmodel/FoodViewModel.kt`

---

## Next Steps

After completing this feature:

1. **Merge to main**: Create PR with tests passing
2. **Update TODO.md**: Mark "Food not appearing after adding to a category" as complete
3. **Plan Phase 2 enhancements**:
   - Edit custom food IBS attributes (long-press)
   - Backfill `commonFoodId` for old FoodItems
   - Add case-insensitive duplicate suggestions

4. **Consider follow-up features**:
   - Category ordering by usage frequency (Quick Win)
   - Input validation (Critical Bug)
   - Error handling in ViewModels (Critical Bug)

---

## Estimated Timeline

| Task | Time | Cumulative |
|------|------|------------|
| Understand codebase | 30 min | 0:30 |
| Write tests | 15 min | 0:45 |
| Implement fix | 30 min | 1:15 |
| Run tests | 10 min | 1:25 |
| Manual testing | 15 min | 1:40 |
| Performance testing | 15 min | 1:55 |
| Code review & fixes | 30 min | 2:25 |
| Documentation | 15 min | 2:40 |

**Total**: ~2.5-3 hours for complete implementation

---

## Success Criteria

- [x] Custom food "Soja" appears in "Other" category immediately after save
- [x] Custom food searchable via search bar
- [x] Duplicate custom food reuses existing CommonFood (no duplicates)
- [x] Usage count increments correctly
- [x] Pre-populated foods still work (no regression)
- [x] Performance: p95 < 500ms for 200 custom foods
- [x] All tests pass (4 new + existing tests)
- [x] No database migration required

---

## Resources

- **DAO Documentation**: `app/src/main/java/.../dao/CommonFoodDao.kt` (inline docs)
- **Migration Example**: `app/src/main/java/.../database/Migration_8_9.kt`
- **Room Best Practices**: [Android Developers - Room](https://developer.android.com/training/data-storage/room)
- **Kotlin Coroutines**: [Kotlinlang.org - Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- **Flow API**: [Android Developers - Flow](https://developer.android.com/kotlin/flow)

---

## Getting Help

If stuck, refer to:
1. **Specification**: `specs/004-fix-custom-food-persistence/spec.md`
2. **Data Model**: `specs/004-fix-custom-food-persistence/data-model.md`
3. **Research Decisions**: `specs/004-fix-custom-food-persistence/research.md`
4. **API Contract**: `specs/004-fix-custom-food-persistence/contracts/DataRepository.kt`
