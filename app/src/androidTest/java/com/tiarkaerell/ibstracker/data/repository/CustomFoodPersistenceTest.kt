package com.tiarkaerell.ibstracker.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
}
