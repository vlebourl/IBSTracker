package com.tiarkaerell.ibstracker.data.analysis

import com.tiarkaerell.ibstracker.data.model.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.max

/**
 * Engine for detecting patterns in symptom and trigger data
 */
class PatternDetectionEngine {
    
    /**
     * Detects temporal patterns in symptom occurrences
     */
    fun detectTemporalPatterns(
        symptoms: List<SymptomOccurrence>,
        foods: List<FoodOccurrence>
    ): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        // Group symptoms by type
        val symptomsByType = symptoms.groupBy { it.type }
        
        symptomsByType.forEach { (symptomType, occurrences) ->
            if (occurrences.size >= 3) {
                patterns.addAll(detectPatternsForSymptom(symptomType, occurrences, foods))
            }
        }
        
        return patterns.sortedByDescending { it.confidence }
    }
    
    /**
     * Detects cyclical patterns (daily, weekly, monthly)
     */
    fun detectCyclicalPatterns(symptoms: List<SymptomOccurrence>): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        if (symptoms.size < 5) return patterns
        
        // Daily patterns (time of day)
        patterns.addAll(detectDailyTimePatterns(symptoms))
        
        // Weekly patterns (day of week)
        patterns.addAll(detectWeeklyPatterns(symptoms))
        
        // Meal timing patterns
        patterns.addAll(detectMealTimingPatterns(symptoms))
        
        return patterns.filter { it.confidence >= 0.3 }
    }
    
    /**
     * Detects trigger combination patterns
     */
    fun detectTriggerCombinations(
        analyses: List<SymptomAnalysis>,
        foods: List<FoodOccurrence>
    ): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        // Detect food combinations that frequently trigger symptoms together
        patterns.addAll(detectFoodCombinationPatterns(analyses, foods))
        
        // Detect timing-based trigger patterns
        patterns.addAll(detectTimingBasedTriggerPatterns(analyses, foods))
        
        return patterns.filter { it.confidence >= 0.4 }
    }
    
    /**
     * Detects severity escalation patterns
     */
    fun detectSeverityPatterns(symptoms: List<SymptomOccurrence>): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        if (symptoms.size < 4) return patterns
        
        val symptomsByType = symptoms.groupBy { it.type }
        
        symptomsByType.forEach { (symptomType, occurrences) ->
            if (occurrences.size >= 4) {
                val sortedOccurrences = occurrences.sortedBy { it.timestamp }
                
                // Detect escalating severity trend
                val escalationPattern = detectSeverityEscalation(symptomType, sortedOccurrences)
                escalationPattern?.let { patterns.add(it) }
                
                // Detect severity cycles
                val cyclicalPattern = detectSeverityCycles(symptomType, sortedOccurrences)
                cyclicalPattern?.let { patterns.add(it) }
            }
        }
        
        return patterns
    }
    
    private fun detectPatternsForSymptom(
        symptomType: String,
        occurrences: List<SymptomOccurrence>,
        foods: List<FoodOccurrence>
    ): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        // Frequency pattern
        val frequencyPattern = detectFrequencyPattern(symptomType, occurrences)
        frequencyPattern?.let { patterns.add(it) }
        
        // Trigger consistency pattern
        val consistencyPattern = detectTriggerConsistency(symptomType, occurrences, foods)
        consistencyPattern?.let { patterns.add(it) }
        
        return patterns
    }
    
    private fun detectFrequencyPattern(
        symptomType: String,
        occurrences: List<SymptomOccurrence>
    ): SymptomPattern? {
        if (occurrences.size < 3) return null
        
        val sortedOccurrences = occurrences.sortedBy { it.timestamp }
        val intervals = mutableListOf<Long>()
        
        for (i in 1 until sortedOccurrences.size) {
            val intervalHours = ChronoUnit.HOURS.between(
                sortedOccurrences[i-1].timestamp,
                sortedOccurrences[i].timestamp
            )
            intervals.add(intervalHours)
        }
        
        val avgInterval = intervals.average()
        val variability = intervals.map { abs(it - avgInterval) }.average()
        val consistency = max(0.0, 1.0 - (variability / avgInterval))
        
        return if (consistency >= 0.4) {
            SymptomPattern(
                id = "freq_${symptomType}_${System.currentTimeMillis()}",
                symptomType = symptomType,
                patternType = PatternType.FREQUENCY,
                description = when {
                    avgInterval <= 24 -> "Daily frequency pattern detected"
                    avgInterval <= 168 -> "Weekly frequency pattern detected"
                    else -> "Regular occurrence pattern detected"
                },
                confidence = consistency,
                occurrenceCount = occurrences.size,
                metadata = mapOf(
                    "avgIntervalHours" to avgInterval,
                    "consistency" to consistency
                )
            )
        } else null
    }
    
    private fun detectTriggerConsistency(
        symptomType: String,
        symptoms: List<SymptomOccurrence>,
        foods: List<FoodOccurrence>
    ): SymptomPattern? {
        if (symptoms.size < 3) return null
        
        val triggerCounts = mutableMapOf<String, Int>()
        
        symptoms.forEach { symptom ->
            // Find foods consumed within 8 hours before symptom
            val triggerFoods = foods.filter { food ->
                val hoursDiff = ChronoUnit.HOURS.between(food.timestamp, symptom.timestamp)
                hoursDiff in 0..8
            }
            
            triggerFoods.forEach { food ->
                triggerCounts[food.name] = triggerCounts.getOrDefault(food.name, 0) + 1
            }
        }
        
        val mostFrequentTrigger = triggerCounts.maxByOrNull { it.value }
        
        return if (mostFrequentTrigger != null && mostFrequentTrigger.value >= symptoms.size * 0.6) {
            val consistency = mostFrequentTrigger.value.toDouble() / symptoms.size
            
            SymptomPattern(
                id = "trigger_${symptomType}_${System.currentTimeMillis()}",
                symptomType = symptomType,
                patternType = PatternType.TRIGGER_CONSISTENCY,
                description = "${mostFrequentTrigger.key} consistently triggers ${symptomType}",
                confidence = consistency,
                occurrenceCount = mostFrequentTrigger.value,
                metadata = mapOf(
                    "triggerFood" to mostFrequentTrigger.key,
                    "triggerCount" to mostFrequentTrigger.value,
                    "totalSymptoms" to symptoms.size
                )
            )
        } else null
    }
    
    private fun detectDailyTimePatterns(symptoms: List<SymptomOccurrence>): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        val timeGroups = symptoms.groupBy { symptom ->
            val hour = LocalDateTime.ofInstant(symptom.timestamp, java.time.ZoneId.systemDefault()).hour
            when (hour) {
                in 6..11 -> "Morning"
                in 12..17 -> "Afternoon"
                in 18..22 -> "Evening"
                else -> "Night"
            }
        }
        
        val dominantTimeOfDay = timeGroups.maxByOrNull { it.value.size }
        
        if (dominantTimeOfDay != null && dominantTimeOfDay.value.size >= symptoms.size * 0.6) {
            val confidence = dominantTimeOfDay.value.size.toDouble() / symptoms.size
            
            patterns.add(SymptomPattern(
                id = "daily_time_${System.currentTimeMillis()}",
                symptomType = "General",
                patternType = PatternType.TEMPORAL,
                description = "Symptoms frequently occur in the ${dominantTimeOfDay.key.lowercase()}",
                confidence = confidence,
                occurrenceCount = dominantTimeOfDay.value.size,
                metadata = mapOf(
                    "timeOfDay" to dominantTimeOfDay.key,
                    "percentage" to (confidence * 100).toInt()
                )
            ))
        }
        
        return patterns
    }
    
    private fun detectWeeklyPatterns(symptoms: List<SymptomOccurrence>): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        val dayGroups = symptoms.groupBy { symptom ->
            LocalDateTime.ofInstant(symptom.timestamp, java.time.ZoneId.systemDefault()).dayOfWeek
        }
        
        val dominantDay = dayGroups.maxByOrNull { it.value.size }
        
        if (dominantDay != null && dominantDay.value.size >= symptoms.size * 0.4) {
            val confidence = dominantDay.value.size.toDouble() / symptoms.size
            
            patterns.add(SymptomPattern(
                id = "weekly_${System.currentTimeMillis()}",
                symptomType = "General",
                patternType = PatternType.TEMPORAL,
                description = "Symptoms commonly occur on ${dominantDay.key}s",
                confidence = confidence,
                occurrenceCount = dominantDay.value.size,
                metadata = mapOf(
                    "dayOfWeek" to dominantDay.key.name,
                    "percentage" to (confidence * 100).toInt()
                )
            ))
        }
        
        return patterns
    }
    
    private fun detectMealTimingPatterns(symptoms: List<SymptomOccurrence>): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        val mealTimeGroups = symptoms.groupBy { symptom ->
            val hour = LocalDateTime.ofInstant(symptom.timestamp, java.time.ZoneId.systemDefault()).hour
            when (hour) {
                in 7..10 -> "Post-Breakfast"
                in 12..15 -> "Post-Lunch"
                in 18..21 -> "Post-Dinner"
                else -> "Between-Meals"
            }
        }
        
        val dominantMealTime = mealTimeGroups.maxByOrNull { it.value.size }
        
        if (dominantMealTime != null && dominantMealTime.value.size >= symptoms.size * 0.5) {
            val confidence = dominantMealTime.value.size.toDouble() / symptoms.size
            
            patterns.add(SymptomPattern(
                id = "meal_timing_${System.currentTimeMillis()}",
                symptomType = "General",
                patternType = PatternType.MEAL_RELATED,
                description = "Symptoms often occur ${dominantMealTime.key.lowercase()}",
                confidence = confidence,
                occurrenceCount = dominantMealTime.value.size,
                metadata = mapOf(
                    "mealRelation" to dominantMealTime.key,
                    "percentage" to (confidence * 100).toInt()
                )
            ))
        }
        
        return patterns
    }
    
    private fun detectFoodCombinationPatterns(
        analyses: List<SymptomAnalysis>,
        foods: List<FoodOccurrence>
    ): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        // Find food combinations that frequently occur together before symptoms
        val combinations = mutableMapOf<Set<String>, Int>()
        
        analyses.forEach { analysis ->
            analysis.triggerProbabilities.forEach { trigger ->
                // Find other foods consumed around the same time
                // Note: This is simplified logic for now
                val relatedFoods = foods.filter { food ->
                    // For now, just group foods consumed within 2 hours of each other
                    true // Simplified logic
                }.map { it.name }.toSet()
                
                if (relatedFoods.size >= 2) {
                    combinations[relatedFoods] = combinations.getOrDefault(relatedFoods, 0) + 1
                }
            }
        }
        
        combinations.filter { it.value >= 3 }.forEach { (foodSet, count) ->
            patterns.add(SymptomPattern(
                id = "combo_${System.currentTimeMillis()}",
                symptomType = "General",
                patternType = PatternType.COMBINATION,
                description = "Food combination pattern: ${foodSet.take(3).joinToString(", ")}",
                confidence = minOf(1.0, count / 10.0),
                occurrenceCount = count,
                metadata = mapOf(
                    "foods" to foodSet.toList(),
                    "combinationCount" to count
                )
            ))
        }
        
        return patterns
    }
    
    private fun detectTimingBasedTriggerPatterns(
        analyses: List<SymptomAnalysis>,
        foods: List<FoodOccurrence>
    ): List<SymptomPattern> {
        val patterns = mutableListOf<SymptomPattern>()
        
        // Detect if symptoms consistently occur at specific intervals after eating
        val timeLags = analyses.flatMap { it.triggerProbabilities }
            .map { it.averageTimeLag.toHours() }
        
        if (timeLags.size >= 5) {
            val avgLag = timeLags.average()
            val consistency = 1.0 - (timeLags.map { abs(it - avgLag) }.average() / avgLag)
            
            if (consistency >= 0.6) {
                patterns.add(SymptomPattern(
                    id = "timing_${System.currentTimeMillis()}",
                    symptomType = "General",
                    patternType = PatternType.TEMPORAL,
                    description = "Symptoms consistently appear ${avgLag.toInt()} hours after eating",
                    confidence = consistency,
                    occurrenceCount = timeLags.size,
                    metadata = mapOf(
                        "averageLagHours" to avgLag,
                        "consistency" to consistency
                    )
                ))
            }
        }
        
        return patterns
    }
    
    private fun detectSeverityEscalation(
        symptomType: String,
        occurrences: List<SymptomOccurrence>
    ): SymptomPattern? {
        if (occurrences.size < 4) return null
        
        val intensities = occurrences.map { it.intensity }
        var increasingTrend = 0
        
        for (i in 1 until intensities.size) {
            if (intensities[i] > intensities[i-1]) {
                increasingTrend++
            }
        }
        
        val trendStrength = increasingTrend.toDouble() / (intensities.size - 1)
        
        return if (trendStrength >= 0.6) {
            SymptomPattern(
                id = "escalation_${symptomType}_${System.currentTimeMillis()}",
                symptomType = symptomType,
                patternType = PatternType.SEVERITY_TREND,
                description = "$symptomType severity shows escalating trend",
                confidence = trendStrength,
                occurrenceCount = occurrences.size,
                metadata = mapOf(
                    "trendDirection" to "increasing",
                    "trendStrength" to trendStrength
                )
            )
        } else null
    }
    
    private fun detectSeverityCycles(
        symptomType: String,
        occurrences: List<SymptomOccurrence>
    ): SymptomPattern? {
        if (occurrences.size < 6) return null
        
        val intensities = occurrences.map { it.intensity }
        val peaks = mutableListOf<Int>()
        
        for (i in 1 until intensities.size - 1) {
            if (intensities[i] > intensities[i-1] && intensities[i] > intensities[i+1]) {
                peaks.add(i)
            }
        }
        
        return if (peaks.size >= 2) {
            val avgCycleLength = if (peaks.size > 1) {
                val intervals = mutableListOf<Int>()
                for (i in 1 until peaks.size) {
                    intervals.add(peaks[i] - peaks[i-1])
                }
                intervals.average()
            } else 0.0
            
            val confidence = minOf(1.0, peaks.size / (intensities.size / 4.0))
            
            SymptomPattern(
                id = "cycle_${symptomType}_${System.currentTimeMillis()}",
                symptomType = symptomType,
                patternType = PatternType.SEVERITY_TREND,
                description = "$symptomType shows cyclical severity pattern",
                confidence = confidence,
                occurrenceCount = peaks.size,
                metadata = mapOf(
                    "peakCount" to peaks.size,
                    "avgCycleLength" to avgCycleLength
                )
            )
        } else null
    }
}