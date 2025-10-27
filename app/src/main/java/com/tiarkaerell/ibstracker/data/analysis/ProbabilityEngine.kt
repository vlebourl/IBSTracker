package com.tiarkaerell.ibstracker.data.analysis

import com.tiarkaerell.ibstracker.data.model.*
import java.time.Duration
import java.time.Instant

class ProbabilityEngine(
    private val correlationCalculator: CorrelationCalculator
) {
    
    companion object {
        private const val MIN_CONFIDENCE_THRESHOLD = 0.2
        private const val MIN_OCCURRENCES_FOR_HIGH_CONFIDENCE = 5
        private const val MIN_OCCURRENCES_FOR_MEDIUM_CONFIDENCE = 3
    }
    
    fun generateSymptomAnalysis(
        symptomType: String,
        symptomOccurrences: List<SymptomOccurrence>,
        foodOccurrences: List<FoodOccurrence>,
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): SymptomAnalysis? {
        
        val filteredSymptoms = filterSymptomsByIntensity(symptomOccurrences, filters.severityThreshold)
        if (filteredSymptoms.isEmpty()) return null
        
        val correlationEvidence = findCorrelationEvidence(filteredSymptoms, foodOccurrences, timeWindow)
        val triggerProbabilities = calculateTriggerProbabilities(correlationEvidence, foodOccurrences, filteredSymptoms.size)
            .filter { it.confidence >= filters.minimumConfidence }
            .sortedByDescending { it.probability }
        
        val recommendationLevel = determineRecommendationLevel(triggerProbabilities)
        if (recommendationLevel == RecommendationLevel.HIDE) return null
        
        val patterns = identifySymptomPatterns(triggerProbabilities, correlationEvidence)
        val insights = generateInsights(triggerProbabilities, patterns, filteredSymptoms, symptomType)
        
        return SymptomAnalysis(
            symptomType = symptomType,
            totalOccurrences = filteredSymptoms.size,
            averageIntensity = if (filteredSymptoms.isNotEmpty()) filteredSymptoms.map { it.intensity }.average() else 0.0,
            severityLevel = calculateSeverityLevel(filteredSymptoms),
            triggerProbabilities = triggerProbabilities,
            patterns = patterns,
            confidence = calculateOverallConfidence(triggerProbabilities),
            recommendationLevel = recommendationLevel,
            lastOccurrence = filteredSymptoms.maxOfOrNull { it.timestamp },
            insights = insights
        )
    }
    
    private fun filterSymptomsByIntensity(
        symptoms: List<SymptomOccurrence>,
        severityThreshold: Int?
    ): List<SymptomOccurrence> {
        return if (severityThreshold != null) {
            symptoms.filter { it.intensity >= severityThreshold }
        } else {
            symptoms
        }
    }
    
    private fun findCorrelationEvidence(
        symptoms: List<SymptomOccurrence>,
        foods: List<FoodOccurrence>,
        timeWindow: AnalysisTimeWindow
    ): Map<String, List<CorrelationEvidence>> {
        val evidence = mutableMapOf<String, MutableList<CorrelationEvidence>>()
        
        for (symptom in symptoms) {
            val eligibleFoods = foods.filter { food ->
                // Only consider foods that occurred BEFORE the symptom (not at the same time)
                food.timestamp.isBefore(symptom.timestamp)
            }.filter { food ->
                val timeLag = Duration.between(food.timestamp, symptom.timestamp)
                timeLag.toHours() in 0..timeWindow.windowSizeHours.toLong()
            }
            
            for (food in eligibleFoods) {
                val timeLag = Duration.between(food.timestamp, symptom.timestamp)
                val temporalWeight = calculateTemporalWeight(timeLag, timeWindow.windowSizeHours)
                
                val correlationEvidence = CorrelationEvidence(
                    foodTimestamp = food.timestamp,
                    symptomTimestamp = symptom.timestamp,
                    timeLag = timeLag,
                    symptomIntensity = symptom.intensity,
                    foodQuantity = food.quantity,
                    temporalWeight = temporalWeight,
                    contextualNotes = food.notes
                )
                
                evidence.getOrPut(food.name) { mutableListOf() }.add(correlationEvidence)
            }
        }
        
        return evidence
    }
    
    private fun calculateTemporalWeight(timeLag: Duration, maxHours: Int): Double {
        val hours = timeLag.toHours().toDouble()
        return (1.0 - (hours / maxHours)).coerceIn(0.0, 1.0)
    }
    
    private fun calculateTriggerProbabilities(
        evidence: Map<String, List<CorrelationEvidence>>,
        allFoods: List<FoodOccurrence>,
        totalSymptomOccurrences: Int
    ): List<TriggerProbability> {
        return evidence.mapNotNull { (foodName, evidenceList) ->
            val ibsTriggerCategory = determineIBSTriggerCategory(foodName)
            val totalFoodOccurrences = allFoods.count { it.name == foodName }
            
            correlationCalculator.calculateTriggerProbability(
                foodName = foodName,
                ibsTriggerCategory = ibsTriggerCategory,
                correlationEvidence = evidenceList,
                totalFoodOccurrences = totalFoodOccurrences,
                totalSymptomOccurrences = totalSymptomOccurrences
            )
        }
    }
    
    private fun determineIBSTriggerCategory(foodName: String): IBSTriggerCategory {
        val lowercaseName = foodName.lowercase()
        
        return when {
            lowercaseName.contains("milk") || lowercaseName.contains("cheese") || 
            lowercaseName.contains("yogurt") || lowercaseName.contains("dairy") -> IBSTriggerCategory.DAIRY
            lowercaseName.contains("wheat") || lowercaseName.contains("bread") || 
            lowercaseName.contains("pasta") || lowercaseName.contains("gluten") -> IBSTriggerCategory.GLUTEN
            lowercaseName.contains("coffee") || lowercaseName.contains("tea") || 
            lowercaseName.contains("caffeine") -> IBSTriggerCategory.CAFFEINE
            lowercaseName.contains("beer") || lowercaseName.contains("wine") || 
            lowercaseName.contains("alcohol") -> IBSTriggerCategory.ALCOHOL
            lowercaseName.contains("spicy") || lowercaseName.contains("hot") || 
            lowercaseName.contains("pepper") -> IBSTriggerCategory.SPICY
            lowercaseName.contains("beans") || lowercaseName.contains("lentils") || 
            lowercaseName.contains("chickpeas") -> IBSTriggerCategory.BEANS_LEGUMES
            lowercaseName.contains("orange") || lowercaseName.contains("lemon") || 
            lowercaseName.contains("lime") || lowercaseName.contains("citrus") -> IBSTriggerCategory.CITRUS
            else -> IBSTriggerCategory.OTHER
        }
    }
    
    private fun determineRecommendationLevel(triggerProbabilities: List<TriggerProbability>): RecommendationLevel {
        if (triggerProbabilities.isEmpty()) return RecommendationLevel.HIDE
        
        val highConfidenceCount = triggerProbabilities.count { it.confidence >= 0.7 && it.occurrenceCount >= MIN_OCCURRENCES_FOR_HIGH_CONFIDENCE }
        val mediumConfidenceCount = triggerProbabilities.count { it.confidence >= 0.5 && it.occurrenceCount >= MIN_OCCURRENCES_FOR_MEDIUM_CONFIDENCE }
        
        return when {
            highConfidenceCount > 0 -> RecommendationLevel.HIGH
            mediumConfidenceCount > 0 -> RecommendationLevel.MEDIUM
            triggerProbabilities.any { it.confidence >= MIN_CONFIDENCE_THRESHOLD } -> RecommendationLevel.LOW_CONFIDENCE
            else -> RecommendationLevel.HIDE
        }
    }
    
    private fun identifySymptomPatterns(
        triggerProbabilities: List<TriggerProbability>,
        evidence: Map<String, List<CorrelationEvidence>>
    ): List<SymptomPattern> {
        return listOf() // Placeholder - pattern detection to be implemented in later task
    }
    
    private fun generateInsights(
        triggerProbabilities: List<TriggerProbability>,
        patterns: List<SymptomPattern>,
        symptoms: List<SymptomOccurrence>,
        symptomType: String
    ): List<String> {
        val insights = mutableListOf<String>()
        
        if (triggerProbabilities.isNotEmpty()) {
            val topTrigger = triggerProbabilities.first()
            insights.add("${topTrigger.foodName} shows the highest correlation (${topTrigger.probabilityPercentage}%) with $symptomType")
        }
        
        val highProbabilityTriggers = triggerProbabilities.filter { it.probability >= 0.7 }
        if (highProbabilityTriggers.size > 1) {
            insights.add("Multiple high-probability triggers identified: ${highProbabilityTriggers.joinToString(", ") { it.foodName }}")
        }
        
        return insights
    }
    
    private fun calculateSeverityLevel(symptoms: List<SymptomOccurrence>): SeverityLevel {
        if (symptoms.isEmpty()) return SeverityLevel.LOW
        val averageIntensity = symptoms.map { it.intensity }.average()
        return when {
            averageIntensity >= 7.0 -> SeverityLevel.HIGH
            averageIntensity >= 4.0 -> SeverityLevel.MEDIUM
            else -> SeverityLevel.LOW
        }
    }
    
    private fun calculateOverallConfidence(triggerProbabilities: List<TriggerProbability>): Double {
        if (triggerProbabilities.isEmpty()) return 0.0
        return triggerProbabilities.map { it.confidence }.average()
    }
    
    /**
     * Formats statistical summary for trigger probabilities
     */
    fun formatTriggerStatistics(trigger: TriggerProbability): String {
        val probabilityText = "${trigger.probabilityPercentage}% probability"
        val occurrenceText = formatOccurrenceCount(trigger.occurrenceCount)
        val confidenceText = formatConfidenceLevel(trigger.confidence)
        val timeLagText = formatAverageTimeLag(trigger.averageTimeLag)
        
        return buildString {
            append(probabilityText)
            append(" • ")
            append(occurrenceText)
            append(" • ")
            append(confidenceText)
            append(" • ")
            append(timeLagText)
        }
    }
    
    /**
     * Formats occurrence count with proper pluralization
     */
    fun formatOccurrenceCount(count: Int): String {
        return when (count) {
            1 -> "1 occurrence"
            else -> "$count occurrences"
        }
    }
    
    /**
     * Formats confidence level with descriptive text
     */
    fun formatConfidenceLevel(confidence: Double): String {
        val percentage = (confidence * 100).toInt()
        val description = when {
            confidence >= 0.9 -> "Very High"
            confidence >= 0.7 -> "High"
            confidence >= 0.5 -> "Moderate"
            confidence >= 0.3 -> "Low"
            else -> "Very Low"
        }
        return "$description confidence ($percentage%)"
    }
    
    /**
     * Formats average time lag in human-readable format
     */
    fun formatAverageTimeLag(duration: java.time.Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        
        return when {
            hours == 0L && minutes < 30 -> "Within 30 min"
            hours == 0L -> "${minutes}min average onset"
            hours == 1L && minutes == 0L -> "1 hour average onset"
            hours < 24 -> "${hours}h ${minutes}min average onset"
            else -> {
                val days = hours / 24
                val remainingHours = hours % 24
                "${days}d ${remainingHours}h average onset"
            }
        }
    }
    
    /**
     * Formats severity level with description
     */
    fun formatSeverityLevel(level: SeverityLevel, averageIntensity: Double): String {
        val intensityText = String.format("%.1f", averageIntensity)
        return when (level) {
            SeverityLevel.HIGH -> "High severity (avg $intensityText/10)"
            SeverityLevel.MEDIUM -> "Moderate severity (avg $intensityText/10)"
            SeverityLevel.LOW -> "Mild severity (avg $intensityText/10)"
        }
    }
    
    /**
     * Formats recommendation level with action text
     */
    fun formatRecommendationLevel(level: RecommendationLevel): String {
        return when (level) {
            RecommendationLevel.HIGH -> "Strong recommendation: Avoid these triggers"
            RecommendationLevel.MEDIUM -> "Moderate recommendation: Monitor these triggers"
            RecommendationLevel.LOW_CONFIDENCE -> "Low confidence: Continue tracking for better insights"
            RecommendationLevel.HIDE -> "Insufficient data for recommendations"
        }
    }
    
    /**
     * Formats a comprehensive summary for a symptom analysis
     */
    fun formatSymptomAnalysisSummary(analysis: SymptomAnalysis): String {
        return buildString {
            append("${analysis.symptomType}: ")
            append(formatOccurrenceCount(analysis.totalOccurrences))
            append(" tracked, ")
            append(formatSeverityLevel(analysis.severityLevel, analysis.averageIntensity))
            
            if (analysis.triggerProbabilities.isNotEmpty()) {
                append(". Top trigger: ${analysis.triggerProbabilities.first().foodName} ")
                append("(${analysis.triggerProbabilities.first().probabilityPercentage}%)")
            }
            
            append(". ")
            append(formatRecommendationLevel(analysis.recommendationLevel))
        }
    }
}

data class SymptomOccurrence(
    val type: String,
    val intensity: Int,
    val timestamp: Instant,
    val notes: String? = null
) {
    init {
        require(type.isNotBlank()) { "Symptom type cannot be blank" }
        require(intensity in 1..10) { "Intensity must be between 1 and 10, was $intensity" }
    }
}

data class FoodOccurrence(
    val name: String,
    val quantity: String? = null,
    val timestamp: Instant,
    val notes: String? = null
) {
    init {
        require(name.isNotBlank()) { "Food name cannot be blank" }
    }
}