package com.tiarkaerell.ibstracker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tiarkaerell.ibstracker.data.backup.BackupManager
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.IBSImpact
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Instrumented tests for Custom Food Persistence Bug Fix.
 *
 * Feature: 004-fix-custom-food-persistence
 * Tests verify that custom foods:
 * - Create CommonFood entries when saved
 * - Appear in category lists
 * - Appear in search results
 * - Do not create duplicates
 */
@RunWith(AndroidJUnit4::class)
class CustomFoodPersistenceTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: DataRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()

        // Create BackupManager for DataRepository
        val backupManager = BackupManager(
            context,
            database,
            10
        )

        repository = DataRepository(
            database.foodItemDao(),
            database.commonFoodDao(),
            database.foodUsageStatsDao(),
            database.symptomDao(),
            backupManager
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    /**
     * Test Case 1: Custom food creates CommonFood entry
     *
     * User Story 1 - Acceptance Scenario 1
     * Given: User adds custom food "Soja" to "OTHER" category
     * When: Food is saved
     * Then: CommonFood entry is created with:
     *   - name = "Soja"
     *   - category = OTHER
     *   - isVerified = false (custom food marker)
     *   - usageCount = 1 (incremented after insert)
     *   - ibsImpacts = [FODMAP_LOW] (default)
     */
    @Test
    fun testCustomFoodCreatesCommonFood() = runBlocking {
        // Arrange
        val customFood = FoodItem(
            name = "Soja",
            quantity = "",
            timestamp = Date(),
            category = FoodCategory.OTHER
        )

        // Act
        repository.insertFoodItem(customFood)

        // Assert
        val commonFood = repository.getCommonFoodByName("Soja").first()
        assertNotNull("CommonFood should be created", commonFood)
        assertEquals("Name should match", "Soja", commonFood?.name)
        assertEquals("Category should match", FoodCategory.OTHER, commonFood?.category)
        assertEquals("Should be marked as custom", false, commonFood?.isVerified)
        assertEquals("Usage count should be 1", 1, commonFood?.usageCount)
        assertTrue(
            "Should have FODMAP_LOW as default",
            commonFood?.ibsImpacts?.contains(IBSImpact.FODMAP_LOW) == true
        )
    }

    /**
     * Test Case 2: Duplicate custom food reuses existing CommonFood
     *
     * User Story 1 - Acceptance Scenario 4
     * Given: Custom food "Soja" already exists
     * When: User adds "Soja" again
     * Then:
     *   - No duplicate CommonFood created
     *   - Usage count incremented to 2
     *   - Only one CommonFood entry exists
     */
    @Test
    fun testDuplicateCustomFoodReusesCommonFood() = runBlocking {
        // Arrange
        val food1 = FoodItem(
            name = "Soja",
            quantity = "",
            timestamp = Date(),
            category = FoodCategory.OTHER
        )
        val food2 = FoodItem(
            name = "Soja",
            quantity = "",
            timestamp = Date(),
            category = FoodCategory.OTHER
        )

        // Act
        repository.insertFoodItem(food1)
        repository.insertFoodItem(food2)

        // Assert - Check only one CommonFood created
        val allCommonFoods = repository.getAllCommonFoods().first()
        val sojaFoods = allCommonFoods.filter { it.name == "Soja" }
        assertEquals("Should have exactly one CommonFood entry", 1, sojaFoods.size)
        assertEquals("Usage count should be 2", 2, sojaFoods[0].usageCount)

        // Assert - Check both FoodItems link to same CommonFood
        val allFoodItems = repository.getAllFoodItems().first()
        val sojaItems = allFoodItems.filter { it.name == "Soja" }
        assertEquals("Should have 2 FoodItem entries", 2, sojaItems.size)
        assertNotNull("First FoodItem should have commonFoodId", sojaItems[0].commonFoodId)
        assertNotNull("Second FoodItem should have commonFoodId", sojaItems[1].commonFoodId)
        assertEquals(
            "Both FoodItems should link to same CommonFood",
            sojaItems[0].commonFoodId,
            sojaItems[1].commonFoodId
        )
    }

    /**
     * Test Case 3: Custom food appears in category list
     *
     * User Story 1 - Acceptance Scenario 2
     * Given: User adds custom food "Soja" to "OTHER" category
     * When: User views "OTHER" category detail screen
     * Then: "Soja" appears in the food list
     */
    @Test
    fun testCustomFoodAppearsInCategoryList() = runBlocking {
        // Arrange
        val customFood = FoodItem(
            name = "Soja",
            quantity = "",
            timestamp = Date(),
            category = FoodCategory.OTHER
        )

        // Act
        repository.insertFoodItem(customFood)

        // Assert
        val categoryFoods = repository.getCommonFoodsByCategory(FoodCategory.OTHER).first()
        val sojaFood = categoryFoods.find { it.name == "Soja" }
        assertNotNull("Soja should appear in OTHER category list", sojaFood)
        assertEquals("Category should be OTHER", FoodCategory.OTHER, sojaFood?.category)
    }

    /**
     * Test Case 4: Custom food appears in search results
     *
     * User Story 1 - Acceptance Scenario 3
     * Given: User adds custom food "Soja"
     * When: User searches for "Soja"
     * Then: "Soja" appears in search results
     */
    @Test
    fun testCustomFoodAppearsInSearch() = runBlocking {
        // Arrange
        val customFood = FoodItem(
            name = "Soja",
            quantity = "",
            timestamp = Date(),
            category = FoodCategory.OTHER
        )

        // Act
        repository.insertFoodItem(customFood)

        // Assert
        val searchResults = repository.searchCommonFoods("Soja").first()
        val sojaFood = searchResults.find { it.name == "Soja" }
        assertNotNull("Soja should appear in search results", sojaFood)
        assertEquals("Name should match search query", "Soja", sojaFood?.name)
    }

    /**
     * Test Case 5: FoodItem linked to CommonFood via commonFoodId
     *
     * Verification test - ensures FoodItem.commonFoodId is set correctly
     */
    @Test
    fun testFoodItemLinkedToCommonFood() = runBlocking {
        // Arrange
        val customFood = FoodItem(
            name = "Tofu",
            quantity = "",
            timestamp = Date(),
            category = FoodCategory.PROTEINS
        )

        // Act
        repository.insertFoodItem(customFood)

        // Assert - Check FoodItem has commonFoodId
        val allFoodItems = repository.getAllFoodItems().first()
        val tofuItem = allFoodItems.find { it.name == "Tofu" }
        assertNotNull("FoodItem should exist", tofuItem)
        assertNotNull("FoodItem should have commonFoodId", tofuItem?.commonFoodId)

        // Assert - Verify commonFoodId links to correct CommonFood
        val commonFood = repository.getCommonFoodById(tofuItem!!.commonFoodId!!).first()
        assertNotNull("CommonFood should exist", commonFood)
        assertEquals("CommonFood name should match", "Tofu", commonFood?.name)
        assertEquals("CommonFood category should match", FoodCategory.PROTEINS, commonFood?.category)
    }

    /**
     * Test Case 6: Sorting with custom foods (User Story 2)
     *
     * User Story 2 - Verification Test
     * Given: Multiple foods with different usage counts
     * When: Category list is queried
     * Then: Foods are sorted by usage_count DESC, then name ASC
     *
     * Expected order:
     * 1. Zucchini (usage: 5)
     * 2. Carrot (usage: 3)
     * 3. Eggplant (usage: 0, alphabetically first)
     * 4. Tomato (usage: 0, alphabetically second)
     */
    @Test
    fun testSortingWithCustomFoods() = runBlocking {
        // Arrange - Create foods with varying usage counts
        val zucchini = FoodItem(name = "Zucchini", quantity = "", timestamp = Date(), category = FoodCategory.VEGETABLES)
        val carrot = FoodItem(name = "Carrot", quantity = "", timestamp = Date(), category = FoodCategory.VEGETABLES)
        val eggplant = FoodItem(name = "Eggplant", quantity = "", timestamp = Date(), category = FoodCategory.VEGETABLES)
        val tomato = FoodItem(name = "Tomato", quantity = "", timestamp = Date(), category = FoodCategory.VEGETABLES)

        // Act - Insert with different usage patterns
        // Zucchini: 5 times
        repeat(5) { repository.insertFoodItem(zucchini.copy(timestamp = Date())) }
        // Carrot: 3 times
        repeat(3) { repository.insertFoodItem(carrot.copy(timestamp = Date())) }
        // Eggplant: 1 time
        repository.insertFoodItem(eggplant)
        // Tomato: 1 time
        repository.insertFoodItem(tomato)

        // Assert - Verify sort order
        val categoryFoods = repository.getCommonFoodsByCategory(FoodCategory.VEGETABLES).first()
        val foodNames = categoryFoods.map { it.name }

        assertTrue("Should have at least 4 foods", categoryFoods.size >= 4)

        // Find indices of our test foods
        val zucchiniIdx = foodNames.indexOf("Zucchini")
        val carrotIdx = foodNames.indexOf("Carrot")
        val eggplantIdx = foodNames.indexOf("Eggplant")
        val tomatoIdx = foodNames.indexOf("Tomato")

        assertTrue("Zucchini should appear before Carrot (higher usage)", zucchiniIdx < carrotIdx)
        assertTrue("Carrot should appear before Eggplant (higher usage)", carrotIdx < eggplantIdx)
        assertTrue("Eggplant should appear before Tomato (alphabetically)", eggplantIdx < tomatoIdx)

        // Verify usage counts
        val zucchiniFood = categoryFoods.find { it.name == "Zucchini" }
        val carrotFood = categoryFoods.find { it.name == "Carrot" }
        assertEquals("Zucchini usage count", 5, zucchiniFood?.usageCount)
        assertEquals("Carrot usage count", 3, carrotFood?.usageCount)
    }

    /**
     * Test Case 7: Category fills to display limit (User Story 2)
     *
     * User Story 2 - Verification Test
     * Given: Category has many foods
     * When: Category list is queried
     * Then: All foods are returned (no artificial limit in DAO)
     *
     * Note: UI applies display limits (e.g., 8 items), but DAO returns all
     */
    @Test
    fun testCategoryFillsToDisplayLimit() = runBlocking {
        // Arrange - Create 10 custom foods in same category
        val foodNames = listOf(
            "Apple", "Banana", "Cherry", "Date", "Elderberry",
            "Fig", "Grape", "Honeydew", "Kiwi", "Lemon"
        )

        foodNames.forEach { name ->
            val food = FoodItem(
                name = name,
                quantity = "",
                timestamp = Date(),
                category = FoodCategory.FRUITS
            )
            repository.insertFoodItem(food)
        }

        // Act
        val categoryFoods = repository.getCommonFoodsByCategory(FoodCategory.FRUITS).first()

        // Assert - DAO returns all foods (no limit)
        assertTrue("Should return at least 10 foods", categoryFoods.size >= 10)

        // Verify all our test foods are present
        val returnedNames = categoryFoods.map { it.name }
        foodNames.forEach { name ->
            assertTrue("Should contain $name", returnedNames.contains(name))
        }
    }

    /**
     * Test Case 8: Mixed pre-populated and custom foods (User Story 2)
     *
     * User Story 2 - Verification Test
     * Given: Category has both pre-populated and custom foods
     * When: Category list is queried
     * Then: Both types appear together, sorted by usage count
     *
     * This simulates real usage where users add custom foods alongside
     * the 72 pre-populated French foods in the database.
     */
    @Test
    fun testMixedPrePopulatedAndCustomFoods() = runBlocking {
        // Arrange - Create mix of pre-populated-style and custom foods
        // Pre-populated style (isVerified would be true in real data, but we create as custom for test)
        val bread = FoodItem(name = "Pain", quantity = "", timestamp = Date(), category = FoodCategory.GRAINS)
        val customBread = FoodItem(name = "Pain complet", quantity = "", timestamp = Date(), category = FoodCategory.GRAINS)

        // Act - Log custom food more than pre-populated
        repeat(3) { repository.insertFoodItem(customBread.copy(timestamp = Date())) }
        repository.insertFoodItem(bread)

        // Assert
        val categoryFoods = repository.getCommonFoodsByCategory(FoodCategory.GRAINS).first()
        val foodNames = categoryFoods.map { it.name }

        // Verify both foods present
        assertTrue("Should contain Pain", foodNames.contains("Pain"))
        assertTrue("Should contain Pain complet", foodNames.contains("Pain complet"))

        // Verify custom food with higher usage appears first
        val painCompletIdx = foodNames.indexOf("Pain complet")
        val painIdx = foodNames.indexOf("Pain")
        assertTrue("Pain complet should appear before Pain (higher usage)", painCompletIdx < painIdx)

        // Verify usage counts
        val painComplet = categoryFoods.find { it.name == "Pain complet" }
        val pain = categoryFoods.find { it.name == "Pain" }
        assertEquals("Pain complet usage", 3, painComplet?.usageCount)
        assertEquals("Pain usage", 1, pain?.usageCount)
    }

    /**
     * Test Case 9: Custom food appears in quick-add (User Story 3)
     *
     * User Story 3 - Verification Test
     * Given: Custom food logged multiple times to become top-used
     * When: Quick-add list is queried (top 4 foods)
     * Then: Custom food appears in quick-add row
     *
     * Use Case: Frequently logged custom foods should appear in quick-add
     * shortcuts alongside pre-populated foods.
     */
    @Test
    fun testCustomFoodAppearsInQuickAdd() = runBlocking {
        // Arrange - Create multiple foods with different usage counts
        val tofu = FoodItem(name = "Tofu", quantity = "", timestamp = Date(), category = FoodCategory.PROTEINS)
        val tempeh = FoodItem(name = "Tempeh", quantity = "", timestamp = Date(), category = FoodCategory.PROTEINS)
        val seitan = FoodItem(name = "Seitan", quantity = "", timestamp = Date(), category = FoodCategory.PROTEINS)
        val edamame = FoodItem(name = "Edamame", quantity = "", timestamp = Date(), category = FoodCategory.PROTEINS)
        val lentils = FoodItem(name = "Lentils", quantity = "", timestamp = Date(), category = FoodCategory.PROTEINS)

        // Act - Log foods with different frequencies
        // Tofu: 15 times (should be #1 in quick-add)
        repeat(15) { repository.insertFoodItem(tofu.copy(timestamp = Date())) }
        // Tempeh: 10 times
        repeat(10) { repository.insertFoodItem(tempeh.copy(timestamp = Date())) }
        // Seitan: 7 times
        repeat(7) { repository.insertFoodItem(seitan.copy(timestamp = Date())) }
        // Edamame: 5 times
        repeat(5) { repository.insertFoodItem(edamame.copy(timestamp = Date())) }
        // Lentils: 3 times (should NOT be in top 4)
        repeat(3) { repository.insertFoodItem(lentils.copy(timestamp = Date())) }

        // Assert - Get top 4 foods
        val topFoods = repository.getTopUsedCommonFoods(limit = 4).first()
        val topFoodNames = topFoods.map { it.name }

        assertEquals("Should return exactly 4 foods", 4, topFoods.size)

        // Verify top 4 in correct order
        assertTrue("Tofu should be in top 4", topFoodNames.contains("Tofu"))
        assertTrue("Tempeh should be in top 4", topFoodNames.contains("Tempeh"))
        assertTrue("Seitan should be in top 4", topFoodNames.contains("Seitan"))
        assertTrue("Edamame should be in top 4", topFoodNames.contains("Edamame"))
        assertFalse("Lentils should NOT be in top 4", topFoodNames.contains("Lentils"))

        // Verify correct order
        assertEquals("Tofu should be #1", "Tofu", topFoodNames[0])
        assertEquals("Tempeh should be #2", "Tempeh", topFoodNames[1])
        assertEquals("Seitan should be #3", "Seitan", topFoodNames[2])
        assertEquals("Edamame should be #4", "Edamame", topFoodNames[3])

        // Verify usage counts
        assertEquals("Tofu usage count", 15, topFoods[0].usageCount)
        assertEquals("Tempeh usage count", 10, topFoods[1].usageCount)
        assertEquals("Seitan usage count", 7, topFoods[2].usageCount)
        assertEquals("Edamame usage count", 5, topFoods[3].usageCount)
    }

    /**
     * Test Case 10: Quick-add updates dynamically with usage (User Story 3)
     *
     * User Story 3 - Verification Test
     * Given: Quick-add showing top 4 foods
     * When: User logs a different food many times
     * Then: Quick-add updates to show new top food
     *
     * This verifies the quick-add is dynamic and reflects real-time usage patterns.
     */
    @Test
    fun testQuickAddUpdatesWithUsage() = runBlocking {
        // Arrange - Create initial top 4 foods
        val food1 = FoodItem(name = "Apple", quantity = "", timestamp = Date(), category = FoodCategory.FRUITS)
        val food2 = FoodItem(name = "Banana", quantity = "", timestamp = Date(), category = FoodCategory.FRUITS)
        val food3 = FoodItem(name = "Cherry", quantity = "", timestamp = Date(), category = FoodCategory.FRUITS)
        val food4 = FoodItem(name = "Date", quantity = "", timestamp = Date(), category = FoodCategory.FRUITS)
        val newFood = FoodItem(name = "Mango", quantity = "", timestamp = Date(), category = FoodCategory.FRUITS)

        // Log initial top 4 foods
        repeat(10) { repository.insertFoodItem(food1.copy(timestamp = Date())) }
        repeat(8) { repository.insertFoodItem(food2.copy(timestamp = Date())) }
        repeat(6) { repository.insertFoodItem(food3.copy(timestamp = Date())) }
        repeat(4) { repository.insertFoodItem(food4.copy(timestamp = Date())) }

        // Act - Verify initial state
        val initialTopFoods = repository.getTopUsedCommonFoods(limit = 4).first()
        val initialNames = initialTopFoods.map { it.name }

        assertEquals("Initial top 4 should have 4 foods", 4, initialTopFoods.size)
        assertFalse("Mango should NOT be in initial top 4", initialNames.contains("Mango"))

        // Log new food enough times to enter top 4 (but not #1)
        repeat(7) { repository.insertFoodItem(newFood.copy(timestamp = Date())) }

        // Assert - Verify Mango now appears in top 4
        val updatedTopFoods = repository.getTopUsedCommonFoods(limit = 4).first()
        val updatedNames = updatedTopFoods.map { it.name }

        assertEquals("Updated top 4 should have 4 foods", 4, updatedTopFoods.size)
        assertTrue("Mango should now be in top 4", updatedNames.contains("Mango"))

        // Verify Mango pushed out the lowest (Date with 4 uses)
        assertFalse("Date should be pushed out of top 4", updatedNames.contains("Date"))

        // Verify new order: Apple (10), Banana (8), Mango (7), Cherry (6)
        assertEquals("Apple should still be #1", "Apple", updatedNames[0])
        assertEquals("Banana should still be #2", "Banana", updatedNames[1])
        assertEquals("Mango should be #3", "Mango", updatedNames[2])
        assertEquals("Cherry should be #4", "Cherry", updatedNames[3])
    }

    /**
     * Test Case 11: Performance with 200 custom foods (Phase 6)
     *
     * Performance Test
     * Given: 200 custom foods in a single category
     * When: Category list is queried
     * Then: Response time is < 500ms (p95)
     *
     * This ensures the app remains responsive with large datasets.
     */
    @Test
    fun testPerformanceWith200CustomFoods() = runBlocking {
        // Arrange - Create 200 custom foods
        val foods = (1..200).map { index ->
            FoodItem(
                name = "CustomFood$index",
                quantity = "",
                timestamp = Date(),
                category = FoodCategory.OTHER
            )
        }

        // Act - Insert all foods and measure time
        val startTime = System.currentTimeMillis()
        foods.forEach { repository.insertFoodItem(it) }
        val insertTime = System.currentTimeMillis() - startTime

        // Query category and measure time
        val queryStartTime = System.currentTimeMillis()
        val categoryFoods = repository.getCommonFoodsByCategory(FoodCategory.OTHER).first()
        val queryTime = System.currentTimeMillis() - queryStartTime

        // Assert - Verify all foods created
        assertTrue("Should have at least 200 foods", categoryFoods.size >= 200)

        // Performance assertion - query should be < 500ms
        assertTrue(
            "Category query should complete in < 500ms (actual: ${queryTime}ms)",
            queryTime < 500
        )

        // Log performance metrics for analysis
        println("Performance Test Results:")
        println("  Insert 200 foods: ${insertTime}ms")
        println("  Query category: ${queryTime}ms")
        println("  Total foods in category: ${categoryFoods.size}")
    }

    /**
     * Test Case 12: Search performance with many foods (Phase 6)
     *
     * Performance Test
     * Given: 100+ foods in database
     * When: Search is performed
     * Then: Response time is < 1s for 50 results
     *
     * Validates search remains fast with large datasets.
     */
    @Test
    fun testSearchPerformanceWithManyFoods() = runBlocking {
        // Arrange - Create 100 foods with searchable names
        val foods = (1..100).map { index ->
            FoodItem(
                name = "SearchTest$index",
                quantity = "",
                timestamp = Date(),
                category = FoodCategory.OTHER
            )
        }

        foods.forEach { repository.insertFoodItem(it) }

        // Act - Search for "SearchTest" and measure time
        val searchStartTime = System.currentTimeMillis()
        val searchResults = repository.searchCommonFoods("SearchTest").first()
        val searchTime = System.currentTimeMillis() - searchStartTime

        // Assert - Verify results limited to 50 (DAO LIMIT)
        assertTrue("Search should return results", searchResults.isNotEmpty())
        assertTrue("Search should limit to 50 results", searchResults.size <= 50)

        // Performance assertion - search should be < 1s
        assertTrue(
            "Search should complete in < 1s (actual: ${searchTime}ms)",
            searchTime < 1000
        )

        // All results should match query
        searchResults.forEach { food ->
            assertTrue(
                "Food name should contain 'SearchTest': ${food.name}",
                food.name.contains("SearchTest", ignoreCase = true)
            )
        }

        println("Search Performance: ${searchTime}ms for ${searchResults.size} results")
    }

    /**
     * Test Case 13: Special characters in food name (Phase 6)
     *
     * Edge Case Test
     * Given: Food name contains UTF-8 special characters
     * When: Food is saved and retrieved
     * Then: Special characters are preserved correctly
     *
     * Validates UTF-8 support for international characters.
     */
    @Test
    fun testSpecialCharactersInFoodName() = runBlocking {
        // Arrange - Foods with various special characters
        val specialFoods = listOf(
            "Café au lait",           // French accents
            "Jalapeño",                // Spanish ñ
            "Crème brûlée",           // Multiple accents
            "Mozzarella di bufala",   // Italian
            "Köttbullar",              // Swedish ö
            "Crêpe",                   // French circumflex
            "Würstchen"                // German umlaut
        )

        // Act - Insert and retrieve each food
        specialFoods.forEach { name ->
            val food = FoodItem(
                name = name,
                quantity = "",
                timestamp = Date(),
                category = FoodCategory.OTHER
            )
            repository.insertFoodItem(food)
        }

        // Assert - Verify all special characters preserved
        val allFoods = repository.getAllCommonFoods().first()

        specialFoods.forEach { expectedName ->
            val found = allFoods.find { it.name == expectedName }
            assertNotNull("Should find food with name: $expectedName", found)
            assertEquals("Name should match exactly", expectedName, found?.name)
        }

        // Verify search works with special characters
        val searchResult = repository.searchCommonFoods("Café").first()
        assertTrue("Should find 'Café au lait'", searchResult.any { it.name == "Café au lait" })
    }

    /**
     * Test Case 14: Case-sensitive duplicates (Phase 6)
     *
     * Edge Case Test
     * Given: Foods with same name but different case
     * When: Both are saved
     * Then: Both entries are created (case-sensitive matching)
     *
     * Validates that "Soja" and "soja" are treated as different foods.
     */
    @Test
    fun testCaseInsensitiveDuplicates() = runBlocking {
        // Arrange - Same name, different case
        val food1 = FoodItem(name = "Soja", quantity = "", timestamp = Date(), category = FoodCategory.OTHER)
        val food2 = FoodItem(name = "soja", quantity = "", timestamp = Date(), category = FoodCategory.OTHER)
        val food3 = FoodItem(name = "SOJA", quantity = "", timestamp = Date(), category = FoodCategory.OTHER)

        // Act - Insert all variations
        repository.insertFoodItem(food1)
        repository.insertFoodItem(food2)
        repository.insertFoodItem(food3)

        // Assert - All three should create separate CommonFood entries
        val allFoods = repository.getAllCommonFoods().first()
        val sojaVariants = allFoods.filter {
            it.name.equals("Soja", ignoreCase = true)
        }

        assertEquals("Should have 3 separate entries for case variations", 3, sojaVariants.size)

        // Verify exact names
        assertTrue("Should have 'Soja'", sojaVariants.any { it.name == "Soja" })
        assertTrue("Should have 'soja'", sojaVariants.any { it.name == "soja" })
        assertTrue("Should have 'SOJA'", sojaVariants.any { it.name == "SOJA" })

        // Each should have usage_count = 1
        sojaVariants.forEach { food ->
            assertEquals("Usage count should be 1 for ${food.name}", 1, food.usageCount)
        }
    }

    /**
     * Test Case 15: Very large usage count (Phase 6)
     *
     * Edge Case Test
     * Given: Food logged 1000+ times
     * When: Sorting is applied
     * Then: Sorting works correctly with large usage counts
     *
     * Validates integer overflow doesn't occur and sorting remains correct.
     */
    @Test
    fun testVeryLargeUsageCount() = runBlocking {
        // Arrange - Create foods with varying large usage counts
        val highUsage = FoodItem(name = "HighUsage", quantity = "", timestamp = Date(), category = FoodCategory.OTHER)
        val mediumUsage = FoodItem(name = "MediumUsage", quantity = "", timestamp = Date(), category = FoodCategory.OTHER)
        val lowUsage = FoodItem(name = "LowUsage", quantity = "", timestamp = Date(), category = FoodCategory.OTHER)

        // Act - Log foods many times
        repeat(1500) { repository.insertFoodItem(highUsage.copy(timestamp = Date())) }  // 1500 uses
        repeat(500) { repository.insertFoodItem(mediumUsage.copy(timestamp = Date())) }  // 500 uses
        repeat(100) { repository.insertFoodItem(lowUsage.copy(timestamp = Date())) }     // 100 uses

        // Assert - Verify correct sorting with large counts
        val categoryFoods = repository.getCommonFoodsByCategory(FoodCategory.OTHER).first()
        val foodNames = categoryFoods.map { it.name }

        val highIdx = foodNames.indexOf("HighUsage")
        val mediumIdx = foodNames.indexOf("MediumUsage")
        val lowIdx = foodNames.indexOf("LowUsage")

        assertTrue("HighUsage should appear before MediumUsage", highIdx < mediumIdx)
        assertTrue("MediumUsage should appear before LowUsage", mediumIdx < lowIdx)

        // Verify usage counts stored correctly (no overflow)
        val highFood = categoryFoods.find { it.name == "HighUsage" }
        val mediumFood = categoryFoods.find { it.name == "MediumUsage" }
        val lowFood = categoryFoods.find { it.name == "LowUsage" }

        assertEquals("HighUsage count", 1500, highFood?.usageCount)
        assertEquals("MediumUsage count", 500, mediumFood?.usageCount)
        assertEquals("LowUsage count", 100, lowFood?.usageCount)

        println("Large Usage Count Test:")
        println("  HighUsage: ${highFood?.usageCount} uses")
        println("  MediumUsage: ${mediumFood?.usageCount} uses")
        println("  LowUsage: ${lowFood?.usageCount} uses")
    }
}
