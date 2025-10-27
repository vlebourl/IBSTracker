package com.tiarkaerell.ibstracker.data.analysis

import com.tiarkaerell.ibstracker.data.model.*
import java.time.Duration

class AnalysisFilterProcessor {
    
    /**
     * Applies filters to a list of symptom analyses
     */
    fun applyFilters(
        analyses: List<SymptomAnalysis>,
        filters: AnalysisFilters
    ): List<SymptomAnalysis> {
        return analyses
            .filter { analysis -> passesFilters(analysis, filters) }
            .map { analysis -> filterAnalysisContent(analysis, filters) }
            .filter { analysis -> hasValidContent(analysis, filters) }
    }
    
    /**
     * Applies filters to symptom occurrences before analysis
     */
    fun filterSymptomOccurrences(
        symptoms: List<SymptomOccurrence>,
        filters: AnalysisFilters
    ): List<SymptomOccurrence> {
        return symptoms.filter { symptom ->
            // Severity threshold filter
            if (filters.severityThreshold != null && symptom.intensity < filters.severityThreshold) {
                return@filter false
            }
            
            // Symptom type filter
            if (filters.symptomTypes.isNotEmpty() && symptom.type !in filters.symptomTypes) {
                return@filter false
            }
            
            true
        }
    }
    
    /**
     * Applies filters to food occurrences before analysis
     */
    fun filterFoodOccurrences(
        foods: List<FoodOccurrence>,
        filters: AnalysisFilters
    ): List<FoodOccurrence> {
        return foods.filter { food ->
            // Exclude foods filter
            if (food.name in filters.excludeFoods) {
                return@filter false
            }
            
            // Food category filter
            if (filters.foodCategories.isNotEmpty()) {
                val foodCategory = determineFoodCategory(food.name)
                if (foodCategory !in filters.foodCategories) {
                    return@filter false
                }
            }
            
            true
        }
    }
    
    /**
     * Checks if a symptom analysis passes the basic filters
     */
    private fun passesFilters(
        analysis: SymptomAnalysis,
        filters: AnalysisFilters
    ): Boolean {
        // Symptom type filter
        if (filters.symptomTypes.isNotEmpty() && analysis.symptomType !in filters.symptomTypes) {
            return false
        }
        
        // Severity level filter (based on average intensity)
        if (filters.severityThreshold != null && analysis.averageIntensity < filters.severityThreshold) {
            return false
        }
        
        return true
    }
    
    /**
     * Filters the content of a symptom analysis (trigger probabilities)
     */
    private fun filterAnalysisContent(
        analysis: SymptomAnalysis,
        filters: AnalysisFilters
    ): SymptomAnalysis {
        val filteredTriggers = analysis.triggerProbabilities.filter { trigger ->
            // Minimum confidence filter
            if (trigger.confidence < filters.minimumConfidence) {
                return@filter false
            }
            
            // Food category filter
            if (filters.foodCategories.isNotEmpty()) {
                val foodCategory = determineFoodCategory(trigger.foodName)
                if (foodCategory !in filters.foodCategories) {
                    return@filter false
                }
            }
            
            // Exclude foods filter
            if (trigger.foodName in filters.excludeFoods) {
                return@filter false
            }
            
            // Low occurrence correlation filter
            if (!filters.showLowOccurrenceCorrelations && trigger.occurrenceCount < 3) {
                return@filter false
            }
            
            true
        }
        
        return analysis.copy(triggerProbabilities = filteredTriggers)
    }
    
    /**
     * Checks if an analysis has valid content after filtering
     */
    private fun hasValidContent(
        analysis: SymptomAnalysis,
        filters: AnalysisFilters
    ): Boolean {
        // If we're showing low occurrence correlations, allow analyses with no triggers
        // as they may still provide valuable insights
        if (filters.showLowOccurrenceCorrelations) {
            return true
        }
        
        // Otherwise, require at least one trigger probability
        return analysis.triggerProbabilities.isNotEmpty()
    }
    
    /**
     * Applies time window filters to analysis results
     */
    fun applyTimeWindowFilters(
        analyses: List<SymptomAnalysis>,
        timeWindow: AnalysisTimeWindow
    ): List<SymptomAnalysis> {
        return analyses.filter { analysis ->
            // Filter by minimum occurrences
            analysis.totalOccurrences >= timeWindow.minimumOccurrences
        }.map { analysis ->
            // Filter trigger probabilities by time lag constraints
            val filteredTriggers = analysis.triggerProbabilities.filter { trigger ->
                // Check if average time lag is within the analysis window
                val timeLagHours = trigger.averageTimeLag.toHours()
                timeLagHours <= timeWindow.windowSizeHours
            }
            
            analysis.copy(triggerProbabilities = filteredTriggers)
        }.filter { analysis ->
            // Keep analyses that still have triggers after time filtering
            analysis.triggerProbabilities.isNotEmpty() || analysis.totalOccurrences >= timeWindow.minimumOccurrences
        }
    }
    
    /**
     * Determines the food category for a given food name
     */
    private fun determineFoodCategory(foodName: String): String {
        // This is a simplified implementation
        // In a real app, you might have a more sophisticated categorization system
        val foodNameLower = foodName.lowercase()
        
        return when {
            // High FODMAP foods
            foodNameLower.contains("apple") || foodNameLower.contains("pear") || 
            foodNameLower.contains("watermelon") || foodNameLower.contains("mango") -> "High FODMAP Fruits"
            
            foodNameLower.contains("onion") || foodNameLower.contains("garlic") || 
            foodNameLower.contains("wheat") || foodNameLower.contains("bread") -> "High FODMAP"
            
            foodNameLower.contains("milk") || foodNameLower.contains("cheese") || 
            foodNameLower.contains("yogurt") || foodNameLower.contains("cream") -> "Dairy"
            
            foodNameLower.contains("bean") || foodNameLower.contains("lentil") || 
            foodNameLower.contains("chickpea") || foodNameLower.contains("kidney") -> "Legumes"
            
            foodNameLower.contains("broccoli") || foodNameLower.contains("cabbage") || 
            foodNameLower.contains("cauliflower") || foodNameLower.contains("brussels") -> "Cruciferous Vegetables"
            
            foodNameLower.contains("beef") || foodNameLower.contains("pork") || 
            foodNameLower.contains("chicken") || foodNameLower.contains("fish") -> "Protein"
            
            foodNameLower.contains("rice") || foodNameLower.contains("pasta") || 
            foodNameLower.contains("potato") || foodNameLower.contains("quinoa") -> "Carbohydrates"
            
            foodNameLower.contains("spicy") || foodNameLower.contains("pepper") || 
            foodNameLower.contains("chili") || foodNameLower.contains("hot") -> "Spicy Foods"
            
            foodNameLower.contains("coffee") || foodNameLower.contains("tea") || 
            foodNameLower.contains("alcohol") || foodNameLower.contains("soda") -> "Beverages"
            
            else -> "Other"
        }
    }
    
    /**
     * Calculates filter match statistics for UI feedback
     */
    fun calculateFilterStats(
        originalAnalyses: List<SymptomAnalysis>,
        filteredAnalyses: List<SymptomAnalysis>,
        filters: AnalysisFilters
    ): FilterStats {
        val originalTriggerCount = originalAnalyses.sumOf { it.triggerProbabilities.size }
        val filteredTriggerCount = filteredAnalyses.sumOf { it.triggerProbabilities.size }
        
        return FilterStats(
            originalAnalysisCount = originalAnalyses.size,
            filteredAnalysisCount = filteredAnalyses.size,
            originalTriggerCount = originalTriggerCount,
            filteredTriggerCount = filteredTriggerCount,
            filteringReduction = if (originalAnalyses.isNotEmpty()) {
                1.0 - (filteredAnalyses.size.toDouble() / originalAnalyses.size)
            } else 0.0,
            activeFilterCount = filters.getActiveFilterCount()
        )
    }
}

/**
 * Statistics about the filtering operation
 */
data class FilterStats(
    val originalAnalysisCount: Int,
    val filteredAnalysisCount: Int,
    val originalTriggerCount: Int,
    val filteredTriggerCount: Int,
    val filteringReduction: Double, // 0.0 to 1.0, percentage of data filtered out
    val activeFilterCount: Int
)

/**
 * Extension function to count active filters
 */
private fun AnalysisFilters.getActiveFilterCount(): Int {
    var count = 0
    if (severityThreshold != null) count++
    if (symptomTypes.isNotEmpty()) count++
    if (foodCategories.isNotEmpty()) count++
    if (excludeFoods.isNotEmpty()) count++
    if (minimumConfidence > 0.0) count++
    if (!showLowOccurrenceCorrelations) count++
    return count
}