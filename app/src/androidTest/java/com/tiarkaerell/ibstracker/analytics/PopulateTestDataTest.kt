package com.tiarkaerell.ibstracker.analytics

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Instrumented test to populate database with realistic test data
 * demonstrating Phase 1 analytics features:
 * - Meal combinations (foods within 30 mins)
 * - Solo foods
 * - Various confidence levels
 * - Co-occurrences
 * - Symptoms within 3-hour window
 *
 * Run this test to populate the emulator database with demo data.
 */
@RunWith(AndroidJUnit4::class)
class PopulateTestDataTest {

    private lateinit var database: AppDatabase
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Use the actual database (not in-memory) to persist data
        database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ibs-tracker-database"
        ).build()
    }

    @Test
    fun populateRealisticTestData() = runBlocking {
        val foodDao = database.foodItemDao()
        val symptomDao = database.symptomDao()

        // Clear existing data first
        foodDao.deleteAll()
        symptomDao.deleteAllSymptoms()

        val now = Date()

        // ========================================
        // Scenario 1: Coffee + Milk combo (HIGH CONFIDENCE - 12 occurrences)
        // This should show as a high-confidence MEAL trigger
        // ========================================
        repeat(12) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((14 - day).toLong()) - TimeUnit.HOURS.toMillis(8))

            // Coffee at 8:00 AM
            foodDao.insert(FoodItem(
                name = "Café",
                category = FoodCategory.BEVERAGES,
                quantity = "1 tasse",
                timestamp = baseTime,
                ibsImpacts = listOf(IBSImpact.CAFFEINE, IBSImpact.ACIDIC)
            ))

            // Milk added 5 minutes later (same meal - within 30 min)
            foodDao.insert(FoodItem(
                name = "Lait",
                category = FoodCategory.DAIRY,
                quantity = "50ml",
                timestamp = Date(baseTime.time + TimeUnit.MINUTES.toMillis(5)),
                ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FODMAP_HIGH)
            ))

            // Symptom 2 hours later (within 3-hour window) for most occurrences
            if (day < 10) { // 10/12 times = 83% trigger rate
                symptomDao.insert(Symptom(
                    name = "Ballonnements",
                    intensity = (3..7).random(),
                    date = Date(baseTime.time + TimeUnit.HOURS.toMillis(2))
                ))
            }
        }

        // ========================================
        // Scenario 2: Toast alone (MODERATE CONFIDENCE - 6 occurrences)
        // Some solo, some with butter - shows isolation tracking
        // ========================================
        repeat(6) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((12 - day).toLong()) - TimeUnit.HOURS.toMillis(7))

            // Toast (solo 3 times, with butter 3 times)
            foodDao.insert(FoodItem(
                name = "Pain blanc",
                category = FoodCategory.GRAINS,
                quantity = "2 tranches",
                timestamp = baseTime,
                ibsImpacts = listOf(IBSImpact.GLUTEN, IBSImpact.FODMAP_MODERATE)
            ))

            // Add butter for half the occurrences (co-occurrence)
            if (day >= 3) {
                foodDao.insert(FoodItem(
                    name = "Beurre",
                    category = FoodCategory.FATS_OILS,
                    quantity = "10g",
                    timestamp = Date(baseTime.time + TimeUnit.MINUTES.toMillis(2)),
                    ibsImpacts = listOf(IBSImpact.FATTY, IBSImpact.LACTOSE)
                ))
            }

            // Symptom 1.5 hours later for some occurrences
            if (day % 2 == 0) { // 3/6 times = 50% trigger rate
                symptomDao.insert(Symptom(
                    name = "Douleur abdominale",
                    intensity = (4..6).random(),
                    date = Date(baseTime.time + TimeUnit.MINUTES.toMillis(90))
                ))
            }
        }

        // ========================================
        // Scenario 3: Banana solo (LOW CONFIDENCE - 4 occurrences)
        // Eaten alone, high trigger rate but low confidence
        // ========================================
        repeat(4) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((10 - day).toLong()) - TimeUnit.HOURS.toMillis(10))

            foodDao.insert(FoodItem(
                name = "Banane",
                category = FoodCategory.FRUITS,
                quantity = "1 moyenne",
                timestamp = baseTime,
                ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE)
            ))

            // 3/4 times triggers symptom (75% but low confidence)
            if (day > 0) {
                symptomDao.insert(Symptom(
                    name = "Gaz",
                    intensity = (2..5).random(),
                    date = Date(baseTime.time + TimeUnit.HOURS.toMillis(2))
                ))
            }
        }

        // ========================================
        // Scenario 4: Big meal combo (MODERATE CONFIDENCE - 7 occurrences)
        // Multiple foods: Chicken + Rice + Vegetables
        // Shows complex meal analysis
        // ========================================
        repeat(7) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((9 - day).toLong()) - TimeUnit.HOURS.toMillis(13))

            // Chicken at 1:00 PM
            foodDao.insert(FoodItem(
                name = "Poulet",
                category = FoodCategory.PROTEINS,
                quantity = "150g",
                timestamp = baseTime,
                ibsImpacts = emptyList()
            ))

            // Rice 2 mins later
            foodDao.insert(FoodItem(
                name = "Riz blanc",
                category = FoodCategory.GRAINS,
                quantity = "1 tasse",
                timestamp = Date(baseTime.time + TimeUnit.MINUTES.toMillis(2)),
                ibsImpacts = listOf(IBSImpact.FODMAP_LOW)
            ))

            // Vegetables 5 mins later
            foodDao.insert(FoodItem(
                name = "Haricots verts",
                category = FoodCategory.VEGETABLES,
                quantity = "1 tasse",
                timestamp = Date(baseTime.time + TimeUnit.MINUTES.toMillis(5)),
                ibsImpacts = listOf(IBSImpact.FODMAP_LOW)
            ))

            // Only 2/7 times causes symptoms (safe meal)
            if (day < 2) {
                symptomDao.insert(Symptom(
                    name = "Ballonnements",
                    intensity = 3,
                    date = Date(baseTime.time + TimeUnit.HOURS.toMillis(2))
                ))
            }
        }

        // ========================================
        // Scenario 5: Apple solo (GOOD CONFIDENCE - 10 occurrences)
        // Always eaten alone, rarely triggers - safe food
        // ========================================
        repeat(10) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((8 - day).toLong()) - TimeUnit.HOURS.toMillis(15))

            foodDao.insert(FoodItem(
                name = "Pomme",
                category = FoodCategory.FRUITS,
                quantity = "1 moyenne",
                timestamp = baseTime,
                ibsImpacts = listOf(IBSImpact.FODMAP_MODERATE)
            ))

            // Only 1/10 times causes symptom (safe food)
            if (day == 5) {
                symptomDao.insert(Symptom(
                    name = "Gaz",
                    intensity = 2,
                    date = Date(baseTime.time + TimeUnit.HOURS.toMillis(2))
                ))
            }
        }

        // ========================================
        // Scenario 6: Cheese + Wine combo (MODERATE CONFIDENCE - 5 occurrences)
        // Shows co-occurrence and high trigger rate
        // ========================================
        repeat(5) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((7 - day).toLong()) - TimeUnit.HOURS.toMillis(19))

            foodDao.insert(FoodItem(
                name = "Fromage",
                category = FoodCategory.DAIRY,
                quantity = "50g",
                timestamp = baseTime,
                ibsImpacts = listOf(IBSImpact.LACTOSE, IBSImpact.FATTY, IBSImpact.FODMAP_HIGH)
            ))

            foodDao.insert(FoodItem(
                name = "Vin rouge",
                category = FoodCategory.BEVERAGES,
                quantity = "1 verre",
                timestamp = Date(baseTime.time + TimeUnit.MINUTES.toMillis(10)),
                ibsImpacts = listOf(IBSImpact.ALCOHOL, IBSImpact.ACIDIC)
            ))

            // 4/5 times causes symptoms (80% trigger rate)
            if (day > 0) {
                symptomDao.insert(Symptom(
                    name = "Douleur abdominale",
                    intensity = (5..8).random(),
                    date = Date(baseTime.time + TimeUnit.HOURS.toMillis(1))
                ))
                symptomDao.insert(Symptom(
                    name = "Diarrhée",
                    intensity = (4..7).random(),
                    date = Date(baseTime.time + TimeUnit.HOURS.toMillis(3))
                ))
            }
        }

        // ========================================
        // Scenario 7: Chocolate solo (VERY LOW CONFIDENCE - 2 occurrences)
        // Shows "need more data" warning
        // ========================================
        repeat(2) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((5 - day).toLong()) - TimeUnit.HOURS.toMillis(16))

            foodDao.insert(FoodItem(
                name = "Chocolat noir",
                category = FoodCategory.SWEETS,
                quantity = "30g",
                timestamp = baseTime,
                ibsImpacts = listOf(IBSImpact.CAFFEINE, IBSImpact.FATTY, IBSImpact.FODMAP_MODERATE)
            ))

            // Both times cause symptoms (100% but very low confidence)
            symptomDao.insert(Symptom(
                name = "Gaz",
                intensity = 4,
                date = Date(baseTime.time + TimeUnit.HOURS.toMillis(2))
            ))
        }

        // ========================================
        // Scenario 8: Add some safe foods with no symptoms (for contrast)
        // ========================================
        repeat(8) { day ->
            val baseTime = Date(now.time - TimeUnit.DAYS.toMillis((6 - day).toLong()) - TimeUnit.HOURS.toMillis(12))

            foodDao.insert(FoodItem(
                name = "Carottes",
                category = FoodCategory.VEGETABLES,
                quantity = "1 tasse",
                timestamp = baseTime,
                ibsImpacts = listOf(IBSImpact.FODMAP_LOW)
            ))

            // No symptoms - safe food
        }

        // Add some random symptoms not linked to any food (background noise)
        repeat(3) { day ->
            symptomDao.insert(Symptom(
                name = "Fatigue",
                intensity = (3..5).random(),
                date = Date(now.time - TimeUnit.DAYS.toMillis((4 - day).toLong()))
            ))
        }

        println("✅ Test data populated successfully!")
        println("📊 Expected results:")
        println("  - High confidence meal triggers: Coffee + Milk (83%)")
        println("  - Moderate confidence meals: Cheese + Wine (80%), Chicken + Rice + Vegetables (29%)")
        println("  - Good confidence solo food: Apple (10% - safe)")
        println("  - Moderate confidence food: Toast (50%)")
        println("  - Low confidence: Banana (75%)")
        println("  - Very low confidence: Chocolate (100%)")
        println("  - Co-occurrences: Toast often with Butter")
        println("  - Isolation tracking: Toast solo vs with butter")
    }
}
