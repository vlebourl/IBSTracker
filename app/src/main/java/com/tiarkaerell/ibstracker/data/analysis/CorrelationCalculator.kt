package com.tiarkaerell.ibstracker.data.analysis

import com.tiarkaerell.ibstracker.data.model.*
import java.time.Duration
import java.time.Instant
import kotlin.math.exp

class CorrelationCalculator {
    
    companion object {
        private const val TEMPORAL_WEIGHT = 0.40
        private const val BASELINE_WEIGHT = 0.30
        private const val FREQUENCY_WEIGHT = 0.30
        private const val MAX_TIME_LAG_HOURS = 8
    }
    
    fun calculateTriggerProbability(
        foodName: String,
        ibsTriggerCategory: IBSTriggerCategory,
        correlationEvidence: List<CorrelationEvidence>,
        totalFoodOccurrences: Int,
        totalSymptomOccurrences: Int
    ): TriggerProbability? {
        if (correlationEvidence.isEmpty() || totalFoodOccurrences == 0) {
            return null
        }
        
        val correlationScore = calculateCorrelationScore(correlationEvidence, totalFoodOccurrences)
        val temporalScore = calculateTemporalScore(correlationEvidence)
        val baselineScore = ibsTriggerCategory.baselineProbability
        val frequencyScore = calculateFrequencyScore(correlationEvidence, totalSymptomOccurrences)
        
        val probability = (temporalScore * TEMPORAL_WEIGHT + 
                          baselineScore * BASELINE_WEIGHT + 
                          frequencyScore * FREQUENCY_WEIGHT).coerceIn(0.0, 1.0)
        
        val confidence = calculateConfidence(correlationEvidence.size, totalFoodOccurrences)
        val averageTimeLag = calculateAverageTimeLag(correlationEvidence)
        val intensityMultiplier = calculateIntensityMultiplier(correlationEvidence)
        
        return TriggerProbability(
            foodName = foodName,
            ibsTriggerCategory = ibsTriggerCategory,
            probability = probability,
            probabilityPercentage = (probability * 100).toInt(),
            confidence = confidence,
            occurrenceCount = correlationEvidence.size,
            correlationScore = correlationScore,
            temporalScore = temporalScore,
            baselineScore = baselineScore,
            frequencyScore = frequencyScore,
            averageTimeLag = averageTimeLag,
            intensityMultiplier = intensityMultiplier,
            lastCorrelationDate = correlationEvidence.maxOf { it.symptomTimestamp },
            supportingEvidence = correlationEvidence
        )
    }
    
    private fun calculateCorrelationScore(
        evidence: List<CorrelationEvidence>,
        totalFoodOccurrences: Int
    ): Double {
        return (evidence.size.toDouble() / totalFoodOccurrences).coerceIn(0.0, 1.0)
    }
    
    private fun calculateTemporalScore(evidence: List<CorrelationEvidence>): Double {
        if (evidence.isEmpty()) return 0.0
        
        val weightedScore = evidence.sumOf { ev ->
            val hoursLag = ev.timeLag.toHours().toDouble()
            val temporalDecay = exp(-hoursLag / MAX_TIME_LAG_HOURS)
            temporalDecay * ev.temporalWeight
        } / evidence.size
        
        return weightedScore.coerceIn(0.0, 1.0)
    }
    
    private fun calculateFrequencyScore(
        evidence: List<CorrelationEvidence>,
        totalSymptomOccurrences: Int
    ): Double {
        if (totalSymptomOccurrences == 0) return 0.0
        return (evidence.size.toDouble() / totalSymptomOccurrences).coerceIn(0.0, 1.0)
    }
    
    private fun calculateConfidence(evidenceCount: Int, totalFoodOccurrences: Int): Double {
        val sampleSizeWeight = (evidenceCount.toDouble() / 10).coerceIn(0.0, 1.0)
        val consistencyWeight = (evidenceCount.toDouble() / totalFoodOccurrences).coerceIn(0.0, 1.0)
        return ((sampleSizeWeight + consistencyWeight) / 2).coerceIn(0.0, 1.0)
    }
    
    private fun calculateAverageTimeLag(evidence: List<CorrelationEvidence>): Duration {
        if (evidence.isEmpty()) return Duration.ZERO
        val totalMinutes = evidence.sumOf { it.timeLag.toMinutes() }
        return Duration.ofMinutes(totalMinutes / evidence.size)
    }
    
    private fun calculateIntensityMultiplier(evidence: List<CorrelationEvidence>): Double {
        if (evidence.isEmpty()) return 1.0
        val averageIntensity = evidence.map { it.symptomIntensity }.average()
        return (averageIntensity / 5.5).coerceIn(0.5, 2.0)
    }
}