package com.tiarkaerell.ibstracker.data.model

import java.time.Duration
import java.time.Instant
import java.util.UUID

data class TriggerProbability(
    val id: String = UUID.randomUUID().toString(),
    val foodName: String,
    val ibsTriggerCategory: IBSTriggerCategory,
    val probability: Double,
    val probabilityPercentage: Int,
    val confidence: Double,
    val occurrenceCount: Int,
    val correlationScore: Double,
    val temporalScore: Double,
    val baselineScore: Double,
    val frequencyScore: Double,
    val averageTimeLag: Duration,
    val intensityMultiplier: Double,
    val lastCorrelationDate: Instant,
    val supportingEvidence: List<CorrelationEvidence>
) {
    init {
        require(probability in 0.0..1.0) { 
            "probability must be between 0.0 and 1.0" 
        }
        require(confidence in 0.0..1.0) { 
            "confidence must be between 0.0 and 1.0" 
        }
        require(probabilityPercentage in 0..100) { 
            "probabilityPercentage must be between 0 and 100" 
        }
        require(probabilityPercentage == (probability * 100).toInt()) {
            "probabilityPercentage should equal (probability * 100).toInt()"
        }
        require(occurrenceCount > 0) { 
            "occurrenceCount must be positive" 
        }
        require(averageTimeLag.toHours() <= 8) { 
            "averageTimeLag must be ≤ 8 hours" 
        }
        require(!averageTimeLag.isNegative) { 
            "averageTimeLag must be positive" 
        }
        require(correlationScore in 0.0..1.0) { 
            "correlationScore must be between 0.0 and 1.0" 
        }
        require(temporalScore in 0.0..1.0) { 
            "temporalScore must be between 0.0 and 1.0" 
        }
        require(baselineScore in 0.0..1.0) { 
            "baselineScore must be between 0.0 and 1.0" 
        }
        require(frequencyScore in 0.0..1.0) { 
            "frequencyScore must be between 0.0 and 1.0" 
        }
    }
}

enum class IBSTriggerCategory(val displayName: String, val baselineProbability: Double) {
    DAIRY("Dairy", 0.65),
    GLUTEN("Gluten", 0.45),
    FODMAP_HIGH("High FODMAP", 0.75),
    CAFFEINE("Caffeine", 0.55),
    ALCOHOL("Alcohol", 0.60),
    SPICY("Spicy Foods", 0.50),
    FATTY("Fatty Foods", 0.58),
    ARTIFICIAL_SWEETENERS("Artificial Sweeteners", 0.70),
    CITRUS("Citrus", 0.40),
    BEANS_LEGUMES("Beans & Legumes", 0.52),
    OTHER("Other", 0.30)
}