package com.tiarkaerell.ibstracker.data.model

import java.util.Date

/**
 * Represents a meal - foods eaten within 30 minutes
 */
data class Meal(
    val foods: List<FoodItem>,
    val timestamp: Date, // Timestamp of first food in meal
    val mealId: String = "${timestamp.time}_${foods.size}" // Unique identifier
) {
    val foodNames: String
        get() = foods.joinToString(" + ") { it.name }
}

/**
 * Confidence level for trigger analysis based on occurrence count
 */
enum class ConfidenceLevel(val displayName: String, val emoji: String) {
    VERY_LOW("Very Low", "⚠️"),
    LOW("Low", "⚠️"),
    MODERATE("Moderate", "⚪"),
    GOOD("Good", "✅"),
    HIGH("High", "✅✅")
}

/**
 * Analysis of a meal (multiple foods eaten together) as a potential trigger
 */
data class MealTrigger(
    val meal: Meal,
    val totalOccurrences: Int, // How many times this meal combination was eaten
    val triggeredOccurrences: Int, // How many times it triggered symptoms
    val triggerPercentage: Float, // (triggeredOccurrences / totalOccurrences) * 100
    val symptomBreakdown: Map<String, Int>, // Symptom name -> count
    val confidence: ConfidenceLevel
)

/**
 * Analysis of a specific food item as a potential trigger
 */
data class FoodItemTrigger(
    val foodName: String,
    val category: FoodCategory,
    val totalOccurrences: Int, // How many times this food was eaten (solo + in meals)
    val triggeredOccurrences: Int, // How many times it triggered symptoms
    val triggerPercentage: Float, // (triggeredOccurrences / totalOccurrences) * 100
    val symptomBreakdown: Map<String, Int>, // Symptom name -> count
    val soloOccurrences: Int, // How many times eaten alone
    val soloTriggered: Int, // How many times triggered when eaten alone
    val mealOccurrences: Int, // How many times eaten in a meal with other foods
    val mealTriggered: Int, // How many times triggered when in a meal
    val coOccurrences: Map<String, Int>, // Other foods often eaten with -> count
    val confidence: ConfidenceLevel
)

/**
 * Analysis of an IBS attribute as a potential trigger
 */
data class IBSAttributeTrigger(
    val attribute: IBSImpact,
    val totalOccurrences: Int, // How many times foods with this attribute were eaten
    val triggeredOccurrences: Int, // How many times they triggered symptoms
    val triggerPercentage: Float, // (triggeredOccurrences / totalOccurrences) * 100
    val symptomBreakdown: Map<String, Int> // Symptom name -> count
)

/**
 * Weekly symptom patterns (kept for trend analysis)
 */
data class WeeklyPattern(
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val symptomCount: Int,
    val averageIntensity: Float
)

/**
 * Complete analytics summary
 */
data class InsightSummary(
    val topMealTriggers: List<MealTrigger>, // Top meal combinations that trigger symptoms (PRIMARY)
    val topFoodTriggers: List<FoodItemTrigger>, // Top food items that trigger symptoms (with isolation data)
    val topAttributeTriggers: List<IBSAttributeTrigger>, // Top IBS attributes that trigger symptoms
    val weeklyPatterns: List<WeeklyPattern>,
    val totalFoodEntries: Int,
    val totalSymptoms: Int,
    val averageSymptomIntensity: Float,
    val daysSinceLastSymptom: Int,
    val improvementTrend: Float // -1.0 to 1.0, positive = improving
)