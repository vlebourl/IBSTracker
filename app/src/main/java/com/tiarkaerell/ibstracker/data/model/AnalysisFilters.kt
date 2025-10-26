package com.tiarkaerell.ibstracker.data.model

data class AnalysisFilters(
    val severityThreshold: Int? = null,
    val symptomTypes: Set<String> = emptySet(),
    val foodCategories: Set<String> = emptySet(),
    val excludeFoods: Set<String> = emptySet(),
    val minimumConfidence: Double = 0.3,
    val showLowOccurrenceCorrelations: Boolean = false
) {
    fun isEmpty(): Boolean {
        return severityThreshold == null && 
               symptomTypes.isEmpty() && 
               foodCategories.isEmpty() && 
               excludeFoods.isEmpty() &&
               minimumConfidence <= 0.3 &&
               !showLowOccurrenceCorrelations
    }
    
    init {
        severityThreshold?.let { threshold ->
            require(threshold in 1..10) { 
                "severityThreshold must be between 1 and 10" 
            }
        }
        require(minimumConfidence in 0.0..1.0) { 
            "minimumConfidence must be between 0.0 and 1.0" 
        }
    }
}