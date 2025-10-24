package com.tiarkaerell.ibstracker.data.analytics

import com.tiarkaerell.ibstracker.data.model.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class AnalyticsEngine {

    companion object {
        private const val SYMPTOM_WINDOW_HOURS = 6 // Look for symptoms within 6 hours of food
        private const val MIN_OCCURRENCES_FOR_ANALYSIS = 3 // Need at least 3 entries to analyze
        private const val RECENT_DAYS = 7
    }

    fun generateInsights(foodItems: List<FoodItem>, symptoms: List<Symptom>): InsightSummary {
        val foodItemTriggers = analyzeFoodItemTriggers(foodItems, symptoms)
        val attributeTriggers = analyzeAttributeTriggers(foodItems, symptoms)
        val weeklyPatterns = analyzeWeeklyPatterns(symptoms)
        val trends = analyzeTrends(symptoms)

        return InsightSummary(
            topFoodTriggers = foodItemTriggers
                .sortedByDescending { it.triggerPercentage }
                .take(10), // Top 10 worst triggers
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
     * Analyze which specific food items trigger symptoms
     * Algorithm:
     * 1. Group food entries by name
     * 2. For each food entry, check if ANY symptom occurred within 6 hours
     * 3. Count triggered vs total occurrences
     * 4. Track which symptoms were triggered
     */
    private fun analyzeFoodItemTriggers(foodItems: List<FoodItem>, symptoms: List<Symptom>): List<FoodItemTrigger> {
        val foodGroups = foodItems.groupBy { it.name }
        val triggers = mutableListOf<FoodItemTrigger>()

        foodGroups.forEach { (foodName, foods) ->
            if (foods.size >= MIN_OCCURRENCES_FOR_ANALYSIS) {
                val symptomCounts = mutableMapOf<String, Int>()
                var triggeredCount = 0

                // For each food entry, check if it triggered any symptom
                foods.forEach { food ->
                    val triggeredSymptoms = symptoms.filter { symptom ->
                        val timeDiff = symptom.date.time - food.timestamp.time
                        timeDiff > 0 && timeDiff <= TimeUnit.HOURS.toMillis(SYMPTOM_WINDOW_HOURS.toLong())
                    }

                    // If this food entry triggered at least one symptom, count it
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
                    FoodItemTrigger(
                        foodName = foodName,
                        category = foods.first().category,
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

    /**
     * Analyze which IBS attributes trigger symptoms
     * Algorithm:
     * 1. For each food entry, get all its IBS impacts
     * 2. Group food entries by each attribute
     * 3. For each attribute, count how many food entries triggered symptoms
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
