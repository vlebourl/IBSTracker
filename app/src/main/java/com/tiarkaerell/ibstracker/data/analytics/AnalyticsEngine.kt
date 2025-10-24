package com.tiarkaerell.ibstracker.data.analytics

import com.tiarkaerell.ibstracker.data.model.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class AnalyticsEngine {
    
    companion object {
        private const val SYMPTOM_WINDOW_HOURS = 6 // Look for symptoms within 6 hours of food
        private const val MIN_OCCURRENCES_FOR_ANALYSIS = 3 // Need at least 3 entries to analyze
        private const val RECENT_DAYS = 7
    }
    
    fun generateInsights(foodItems: List<FoodItem>, symptoms: List<Symptom>): InsightSummary {
        val triggerAnalysis = analyzeTriggers(foodItems, symptoms)
        val categoryInsights = analyzeCategoryInsights(foodItems, symptoms)
        val weeklyPatterns = analyzeWeeklyPatterns(symptoms)
        val trends = analyzeTrends(symptoms)
        
        return InsightSummary(
            topTriggers = triggerAnalysis.sortedByDescending { it.triggerScore }.take(3),
            safestCategories = categoryInsights.sortedByDescending { it.safetyScore }.take(3),
            weeklyPatterns = weeklyPatterns,
            totalFoodEntries = foodItems.size,
            totalSymptoms = symptoms.size,
            averageSymptomIntensity = symptoms.map { it.intensity }.average().toFloat().takeIf { !it.isNaN() } ?: 0f,
            daysSinceLastSymptom = calculateDaysSinceLastSymptom(symptoms),
            improvementTrend = trends
        )
    }
    
    private fun analyzeTriggers(foodItems: List<FoodItem>, symptoms: List<Symptom>): List<TriggerAnalysis> {
        val categoryGroups = foodItems.groupBy { it.category }
        val triggerAnalysis = mutableListOf<TriggerAnalysis>()
        
        categoryGroups.forEach { (category, foods) ->
            if (foods.size >= MIN_OCCURRENCES_FOR_ANALYSIS) {
                val symptomsTriggered = mutableListOf<Pair<Symptom, Long>>()
                
                foods.forEach { food ->
                    val symptomsAfterFood = symptoms.filter { symptom ->
                        val timeDiff = symptom.date.time - food.timestamp.time
                        timeDiff > 0 && timeDiff <= TimeUnit.HOURS.toMillis(SYMPTOM_WINDOW_HOURS.toLong())
                    }
                    
                    symptomsAfterFood.forEach { symptom ->
                        val timeToSymptom = symptom.date.time - food.timestamp.time
                        symptomsTriggered.add(symptom to timeToSymptom)
                    }
                }
                
                val triggerScore = if (foods.isNotEmpty()) {
                    min(1.0f, symptomsTriggered.size.toFloat() / foods.size.toFloat())
                } else 0f
                
                val avgTimeToSymptom = if (symptomsTriggered.isNotEmpty()) {
                    symptomsTriggered.map { it.second }.average().toLong()
                } else 0L
                
                triggerAnalysis.add(
                    TriggerAnalysis(
                        category = category,
                        triggerScore = triggerScore,
                        occurrences = foods.size,
                        symptomsTriggered = symptomsTriggered.size,
                        averageTimeToSymptom = avgTimeToSymptom
                    )
                )
            }
        }
        
        return triggerAnalysis
    }
    
    private fun analyzeCategoryInsights(foodItems: List<FoodItem>, symptoms: List<Symptom>): List<CategoryInsight> {
        val sevenDaysAgo = Calendar.getInstance().apply { 
            add(Calendar.DAY_OF_YEAR, -RECENT_DAYS) 
        }.time
        
        return FoodCategory.getAllCategories().map { category ->
            val categoryFoods = foodItems.filter { it.category == category }
            val recentFoods = categoryFoods.filter { it.timestamp.after(sevenDaysAgo) }
            
            // Calculate safety score (inverse of trigger likelihood)
            val triggerEvents = categoryFoods.count { food ->
                symptoms.any { symptom ->
                    val timeDiff = symptom.date.time - food.timestamp.time
                    timeDiff > 0 && timeDiff <= TimeUnit.HOURS.toMillis(SYMPTOM_WINDOW_HOURS.toLong())
                }
            }
            
            val safetyScore = if (categoryFoods.isNotEmpty()) {
                max(0f, 1f - (triggerEvents.toFloat() / categoryFoods.size.toFloat()))
            } else 0.5f // Neutral for categories with no data
            
            CategoryInsight(
                category = category,
                totalEntries = categoryFoods.size,
                recentEntries = recentFoods.size,
                safetyScore = safetyScore
            )
        }.filter { it.totalEntries > 0 } // Only include categories with data
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