package com.tiarkaerell.ibstracker.data.model

import java.util.Date

data class TriggerAnalysis(
    val category: FoodCategory,
    val triggerScore: Float, // 0.0 to 1.0, higher = more likely trigger
    val occurrences: Int,
    val symptomsTriggered: Int,
    val averageTimeToSymptom: Long // milliseconds
)

data class WeeklyPattern(
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val symptomCount: Int,
    val averageIntensity: Float
)

data class CategoryInsight(
    val category: FoodCategory,
    val totalEntries: Int,
    val recentEntries: Int, // last 7 days
    val safetyScore: Float // 0.0 = high trigger, 1.0 = safe
)

data class InsightSummary(
    val topTriggers: List<TriggerAnalysis>,
    val safestCategories: List<CategoryInsight>,
    val weeklyPatterns: List<WeeklyPattern>,
    val totalFoodEntries: Int,
    val totalSymptoms: Int,
    val averageSymptomIntensity: Float,
    val daysSinceLastSymptom: Int,
    val improvementTrend: Float // -1.0 to 1.0, positive = improving
)