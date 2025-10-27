package com.tiarkaerell.ibstracker.data.model

import java.time.Instant
import java.util.UUID

data class SymptomAnalysis(
    val id: String = UUID.randomUUID().toString(),
    val symptomType: String,
    val totalOccurrences: Int,
    val averageIntensity: Double,
    val severityLevel: SeverityLevel,
    val triggerProbabilities: List<TriggerProbability>,
    val patterns: List<SymptomPattern>,
    val confidence: Double,
    val recommendationLevel: RecommendationLevel,
    val lastOccurrence: Instant?,
    val insights: List<String>
) {
    init {
        require(totalOccurrences > 0) { 
            "totalOccurrences must be positive" 
        }
        require(averageIntensity in 1.0..10.0) { 
            "averageIntensity must be between 1.0 and 10.0" 
        }
        require(confidence in 0.0..1.0) { 
            "confidence must be between 0.0 and 1.0" 
        }
        require(triggerProbabilities == triggerProbabilities.sortedByDescending { it.probability }) {
            "triggerProbabilities must be sorted by probability descending"
        }
        require(recommendationLevel != RecommendationLevel.HIDE || triggerProbabilities.isEmpty()) {
            "If recommendationLevel is HIDE, triggerProbabilities should be empty"
        }
    }
}

enum class SeverityLevel { LOW, MEDIUM, HIGH }

enum class RecommendationLevel { 
    HIDE,           // Don't show - insufficient data
    LOW_CONFIDENCE, // Show with warnings
    MEDIUM,         // Show as potential pattern
    HIGH            // Show as likely pattern
}