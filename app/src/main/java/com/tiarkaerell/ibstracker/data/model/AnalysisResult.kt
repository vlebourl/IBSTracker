package com.tiarkaerell.ibstracker.data.model

import java.time.Instant
import java.util.UUID

data class AnalysisResult(
    val id: String = UUID.randomUUID().toString(),
    val generatedAt: Instant,
    val analysisTimeWindow: AnalysisTimeWindow,
    val filters: AnalysisFilters,
    val symptomAnalyses: List<SymptomAnalysis>,
    val totalSymptomOccurrences: Int,
    val totalFoodEntries: Int,
    val observationPeriodDays: Int,
    val reliabilityScore: Double
) {
    init {
        require(!generatedAt.isAfter(Instant.now())) { 
            "generatedAt must not be in the future" 
        }
        require(totalSymptomOccurrences <= 0 || symptomAnalyses.isNotEmpty()) { 
            "symptomAnalyses must not be empty if totalSymptomOccurrences > 0" 
        }
        require(reliabilityScore in 0.0..1.0) { 
            "reliabilityScore must be between 0.0 and 1.0" 
        }
        require(observationPeriodDays > 0) { 
            "observationPeriodDays must be positive" 
        }
    }
}