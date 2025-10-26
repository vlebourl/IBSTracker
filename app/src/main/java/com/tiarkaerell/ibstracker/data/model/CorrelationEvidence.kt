package com.tiarkaerell.ibstracker.data.model

import java.time.Duration
import java.time.Instant
import java.util.UUID

data class CorrelationEvidence(
    val id: String = UUID.randomUUID().toString(),
    val foodTimestamp: Instant,
    val symptomTimestamp: Instant,
    val timeLag: Duration,
    val symptomIntensity: Int,
    val foodQuantity: String?,
    val temporalWeight: Double,
    val contextualNotes: String?
) {
    init {
        require(symptomTimestamp.isAfter(foodTimestamp)) { 
            "symptomTimestamp must be after foodTimestamp" 
        }
        require(timeLag == Duration.between(foodTimestamp, symptomTimestamp)) {
            "timeLag must equal the difference between timestamps"
        }
        require(symptomIntensity in 1..10) { 
            "symptomIntensity must be between 1 and 10" 
        }
        require(temporalWeight in 0.0..1.0) { 
            "temporalWeight must be between 0.0 and 1.0" 
        }
    }
}