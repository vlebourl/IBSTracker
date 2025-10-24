package com.tiarkaerell.ibstracker.data.analytics

import com.tiarkaerell.ibstracker.data.model.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class AnalyticsEngine {

    companion object {
        private const val SYMPTOM_WINDOW_HOURS = 3 // Look for symptoms within 3 hours of food (clinical standard)
        private const val MEAL_GROUPING_MINUTES = 30 // Foods within 30 minutes = same meal
        private const val MIN_OCCURRENCES_FOR_ANALYSIS = 3 // Need at least 3 entries to analyze
        private const val RECENT_DAYS = 7
    }

    fun generateInsights(foodItems: List<FoodItem>, symptoms: List<Symptom>): InsightSummary {
        // Group foods into meals
        val meals = groupIntoMeals(foodItems)

        // Analyze triggers
        val mealTriggers = analyzeMealTriggers(meals, symptoms)
        val foodItemTriggers = analyzeFoodItemTriggers(foodItems, meals, symptoms)
        val attributeTriggers = analyzeAttributeTriggers(foodItems, symptoms)
        val weeklyPatterns = analyzeWeeklyPatterns(symptoms)
        val trends = analyzeTrends(symptoms)

        return InsightSummary(
            topMealTriggers = mealTriggers
                .sortedByDescending { it.triggerPercentage }
                .take(10), // Top 10 meal triggers
            topFoodTriggers = foodItemTriggers
                .sortedByDescending { it.triggerPercentage }
                .take(10), // Top 10 food triggers
            topAttributeTriggers = attributeTriggers
                .sortedByDescending { it.triggerPercentage }
                .take(10), // Top 10 attribute triggers
            weeklyPatterns = weeklyPatterns,
            totalFoodEntries = foodItems.size,
            totalSymptoms = symptoms.size,
            averageSymptomIntensity = symptoms.map { it.intensity }.average().toFloat().takeIf { !it.isNaN() } ?: 0f,
            daysSinceLastSymptom = calculateDaysSinceLastSymptom(symptoms),
            improvementTrend = trends
        )
    }

    /**
     * Group food items into meals
     * Foods eaten within 30 minutes = same meal
     */
    private fun groupIntoMeals(foodItems: List<FoodItem>): List<Meal> {
        if (foodItems.isEmpty()) return emptyList()

        val sortedFoods = foodItems.sortedBy { it.timestamp }
        val meals = mutableListOf<Meal>()
        var currentMealFoods = mutableListOf<FoodItem>()

        sortedFoods.forEachIndexed { index, food ->
            if (currentMealFoods.isEmpty()) {
                // Start new meal
                currentMealFoods.add(food)
            } else {
                // Check if within 30 minutes of last food in current meal
                val timeDiff = food.timestamp.time - currentMealFoods.last().timestamp.time
                val minutesDiff = TimeUnit.MILLISECONDS.toMinutes(timeDiff)

                if (minutesDiff <= MEAL_GROUPING_MINUTES) {
                    // Add to current meal
                    currentMealFoods.add(food)
                } else {
                    // Finish current meal and start new one
                    if (currentMealFoods.isNotEmpty()) {
                        meals.add(Meal(currentMealFoods.toList(), currentMealFoods.first().timestamp))
                    }
                    currentMealFoods = mutableListOf(food)
                }
            }

            // Add last meal
            if (index == sortedFoods.lastIndex && currentMealFoods.isNotEmpty()) {
                meals.add(Meal(currentMealFoods.toList(), currentMealFoods.first().timestamp))
            }
        }

        return meals
    }

    /**
     * Analyze which meals trigger symptoms
     * PRIMARY analysis - shown first in UI
     */
    private fun analyzeMealTriggers(meals: List<Meal>, symptoms: List<Symptom>): List<MealTrigger> {
        // Group meals by food combination (same foods = same meal type)
        val mealGroups = meals.groupBy { it.foodNames }
        val triggers = mutableListOf<MealTrigger>()

        mealGroups.forEach { (mealName, mealsList) ->
            if (mealsList.size >= MIN_OCCURRENCES_FOR_ANALYSIS) {
                val symptomCounts = mutableMapOf<String, Int>()
                var triggeredCount = 0

                // For each meal occurrence, check if it triggered symptoms
                mealsList.forEach { meal ->
                    val triggeredSymptoms = symptoms.filter { symptom ->
                        val timeDiff = symptom.date.time - meal.timestamp.time
                        timeDiff > 0 && timeDiff <= TimeUnit.HOURS.toMillis(SYMPTOM_WINDOW_HOURS.toLong())
                    }

                    if (triggeredSymptoms.isNotEmpty()) {
                        triggeredCount++

                        // Track which symptoms were triggered
                        triggeredSymptoms.forEach { symptom ->
                            symptomCounts[symptom.name] = (symptomCounts[symptom.name] ?: 0) + 1
                        }
                    }
                }

                val percentage = if (mealsList.isNotEmpty()) {
                    (triggeredCount.toFloat() / mealsList.size.toFloat()) * 100f
                } else 0f

                triggers.add(
                    MealTrigger(
                        meal = mealsList.first(), // Use first occurrence as representative
                        totalOccurrences = mealsList.size,
                        triggeredOccurrences = triggeredCount,
                        triggerPercentage = percentage,
                        symptomBreakdown = symptomCounts,
                        confidence = calculateConfidence(mealsList.size)
                    )
                )
            }
        }

        return triggers
    }

    /**
     * Analyze which individual food items trigger symptoms
     * Enhanced with: isolation tracking, co-occurrence, confidence levels
     */
    private fun analyzeFoodItemTriggers(
        foodItems: List<FoodItem>,
        meals: List<Meal>,
        symptoms: List<Symptom>
    ): List<FoodItemTrigger> {
        val foodGroups = foodItems.groupBy { it.name }
        val triggers = mutableListOf<FoodItemTrigger>()

        foodGroups.forEach { (foodName, foods) ->
            if (foods.size >= MIN_OCCURRENCES_FOR_ANALYSIS) {
                val symptomCounts = mutableMapOf<String, Int>()
                val coOccurrences = mutableMapOf<String, Int>()
                var triggeredCount = 0
                var soloOccurrences = 0
                var soloTriggered = 0
                var mealOccurrences = 0
                var mealTriggered = 0

                // For each food entry, check if it triggered symptoms
                foods.forEach { food ->
                    // Determine if this food was eaten solo or in a meal
                    val mealContainingFood = meals.find { meal ->
                        meal.foods.any { it.id == food.id }
                    }

                    val isEatenSolo = mealContainingFood?.foods?.size == 1

                    if (isEatenSolo) {
                        soloOccurrences++
                    } else {
                        mealOccurrences++

                        // Track co-occurrences (other foods in same meal)
                        mealContainingFood?.foods?.forEach { otherFood ->
                            if (otherFood.name != foodName) {
                                coOccurrences[otherFood.name] = (coOccurrences[otherFood.name] ?: 0) + 1
                            }
                        }
                    }

                    val triggeredSymptoms = symptoms.filter { symptom ->
                        val timeDiff = symptom.date.time - food.timestamp.time
                        timeDiff > 0 && timeDiff <= TimeUnit.HOURS.toMillis(SYMPTOM_WINDOW_HOURS.toLong())
                    }

                    if (triggeredSymptoms.isNotEmpty()) {
                        triggeredCount++

                        if (isEatenSolo) {
                            soloTriggered++
                        } else {
                            mealTriggered++
                        }

                        // Track which symptoms were triggered
                        triggeredSymptoms.forEach { symptom ->
                            symptomCounts[symptom.name] = (symptomCounts[symptom.name] ?: 0) + 1
                        }
                    }
                }

                val percentage = if (foods.isNotEmpty()) {
                    (triggeredCount.toFloat() / foods.size.toFloat()) * 100f
                } else 0f

                triggers.add(
                    FoodItemTrigger(
                        foodName = foodName,
                        category = foods.first().category,
                        totalOccurrences = foods.size,
                        triggeredOccurrences = triggeredCount,
                        triggerPercentage = percentage,
                        symptomBreakdown = symptomCounts,
                        soloOccurrences = soloOccurrences,
                        soloTriggered = soloTriggered,
                        mealOccurrences = mealOccurrences,
                        mealTriggered = mealTriggered,
                        coOccurrences = coOccurrences.toList()
                            .sortedByDescending { it.second }
                            .take(5) // Top 5 co-occurrences
                            .toMap(),
                        confidence = calculateConfidence(foods.size)
                    )
                )
            }
        }

        return triggers
    }

    /**
     * Calculate confidence level based on occurrence count
     */
    private fun calculateConfidence(occurrences: Int): ConfidenceLevel {
        return when (occurrences) {
            in 1..2 -> ConfidenceLevel.VERY_LOW
            in 3..4 -> ConfidenceLevel.LOW
            in 5..9 -> ConfidenceLevel.MODERATE
            in 10..14 -> ConfidenceLevel.GOOD
            else -> ConfidenceLevel.HIGH
        }
    }

    /**
     * Analyze which IBS attributes trigger symptoms
     */
    private fun analyzeAttributeTriggers(foodItems: List<FoodItem>, symptoms: List<Symptom>): List<IBSAttributeTrigger> {
        // Create a map of attribute -> list of food items with that attribute
        val attributeFoodsMap = mutableMapOf<IBSImpact, MutableList<FoodItem>>()

        foodItems.forEach { food ->
            food.ibsImpacts.forEach { attribute ->
                attributeFoodsMap.getOrPut(attribute) { mutableListOf() }.add(food)
            }
        }

        val triggers = mutableListOf<IBSAttributeTrigger>()

        attributeFoodsMap.forEach { (attribute, foods) ->
            if (foods.size >= MIN_OCCURRENCES_FOR_ANALYSIS) {
                val symptomCounts = mutableMapOf<String, Int>()
                var triggeredCount = 0

                // For each food entry with this attribute, check if it triggered symptoms
                foods.forEach { food ->
                    val triggeredSymptoms = symptoms.filter { symptom ->
                        val timeDiff = symptom.date.time - food.timestamp.time
                        timeDiff > 0 && timeDiff <= TimeUnit.HOURS.toMillis(SYMPTOM_WINDOW_HOURS.toLong())
                    }

                    if (triggeredSymptoms.isNotEmpty()) {
                        triggeredCount++

                        // Track which symptoms were triggered
                        triggeredSymptoms.forEach { symptom ->
                            symptomCounts[symptom.name] = (symptomCounts[symptom.name] ?: 0) + 1
                        }
                    }
                }

                val percentage = if (foods.isNotEmpty()) {
                    (triggeredCount.toFloat() / foods.size.toFloat()) * 100f
                } else 0f

                triggers.add(
                    IBSAttributeTrigger(
                        attribute = attribute,
                        totalOccurrences = foods.size,
                        triggeredOccurrences = triggeredCount,
                        triggerPercentage = percentage,
                        symptomBreakdown = symptomCounts
                    )
                )
            }
        }

        return triggers
    }

    private fun analyzeWeeklyPatterns(symptoms: List<Symptom>): List<WeeklyPattern> {
        val calendar = Calendar.getInstance()
        val dayGroups = symptoms.groupBy { symptom ->
            calendar.time = symptom.date
            calendar.get(Calendar.DAY_OF_WEEK)
        }

        return (Calendar.SUNDAY..Calendar.SATURDAY).map { dayOfWeek ->
            val daySymptoms = dayGroups[dayOfWeek] ?: emptyList()
            WeeklyPattern(
                dayOfWeek = dayOfWeek,
                symptomCount = daySymptoms.size,
                averageIntensity = daySymptoms.map { it.intensity }.average().toFloat().takeIf { !it.isNaN() } ?: 0f
            )
        }
    }

    private fun analyzeTrends(symptoms: List<Symptom>): Float {
        if (symptoms.size < 4) return 0f // Need sufficient data

        val sortedSymptoms = symptoms.sortedBy { it.date }
        val recentHalf = sortedSymptoms.takeLast(sortedSymptoms.size / 2)
        val earlierHalf = sortedSymptoms.take(sortedSymptoms.size / 2)

        val recentAvgIntensity = recentHalf.map { it.intensity }.average()
        val earlierAvgIntensity = earlierHalf.map { it.intensity }.average()

        // Negative trend = improvement (lower intensity), positive = worsening
        val intensityTrend = (recentAvgIntensity - earlierAvgIntensity) / 10.0 // Normalize to -1 to 1

        return -intensityTrend.toFloat().coerceIn(-1f, 1f) // Invert so positive = improvement
    }

    private fun calculateDaysSinceLastSymptom(symptoms: List<Symptom>): Int {
        if (symptoms.isEmpty()) return Int.MAX_VALUE

        val lastSymptom = symptoms.maxByOrNull { it.date }
        return if (lastSymptom != null) {
            val daysDiff = (Date().time - lastSymptom.date.time) / TimeUnit.DAYS.toMillis(1)
            daysDiff.toInt()
        } else Int.MAX_VALUE
    }
}
