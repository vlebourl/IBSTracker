package com.tiarkaerell.ibstracker.data.model

import java.util.Date

/**
 * Analysis of a specific food item as a potential trigger
 */
data class FoodItemTrigger(
    val foodName: String,
    val category: FoodCategory,
    val totalOccurrences: Int, // How many times this food was eaten
    val triggeredOccurrences: Int, // How many times it triggered symptoms
    val triggerPercentage: Float, // (triggeredOccurrences / totalOccurrences) * 100
    val symptomBreakdown: Map<String, Int> // Symptom name -> count
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
    val topFoodTriggers: List<FoodItemTrigger>, // Top food items that trigger symptoms
    val topAttributeTriggers: List<IBSAttributeTrigger>, // Top IBS attributes that trigger symptoms
    val weeklyPatterns: List<WeeklyPattern>,
    val totalFoodEntries: Int,
    val totalSymptoms: Int,
    val averageSymptomIntensity: Float,
    val daysSinceLastSymptom: Int,
    val improvementTrend: Float // -1.0 to 1.0, positive = improving
)