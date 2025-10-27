package com.tiarkaerell.ibstracker.data.model

/**
 * Represents a detected pattern in symptom data
 */
data class SymptomPattern(
    val id: String,
    val symptomType: String,
    val patternType: PatternType,
    val description: String,
    val confidence: Double, // 0.0 to 1.0
    val occurrenceCount: Int,
    val metadata: Map<String, Any> = emptyMap()
) {
    val confidencePercentage: Int
        get() = (confidence * 100).toInt()
    
    val isHighConfidence: Boolean
        get() = confidence >= 0.7
    
    val isModerateConfidence: Boolean
        get() = confidence >= 0.4 && confidence < 0.7
    
    val isLowConfidence: Boolean
        get() = confidence < 0.4
}

/**
 * Types of patterns that can be detected in symptom data
 */
enum class PatternType(val displayName: String, val description: String) {
    FREQUENCY(
        "Frequency Pattern",
        "Regular timing intervals between symptom occurrences"
    ),
    TEMPORAL(
        "Time-based Pattern", 
        "Symptoms occurring at specific times of day or days of week"
    ),
    TRIGGER_CONSISTENCY(
        "Trigger Consistency",
        "Same foods consistently triggering symptoms"
    ),
    SEVERITY_TREND(
        "Severity Trend",
        "Escalating or cyclical severity patterns over time"
    ),
    COMBINATION(
        "Food Combination",
        "Multiple foods consumed together triggering symptoms"
    ),
    MEAL_RELATED(
        "Meal Timing",
        "Symptoms related to specific meal times"
    ),
    CATEGORY_PREFERENCE(
        "Category Pattern",
        "Specific food categories showing strong trigger correlations"
    ),
    SEASONAL(
        "Seasonal Pattern",
        "Symptoms varying by season or weather conditions"
    )
}

/**
 * Confidence level categorization for patterns
 */
enum class PatternConfidenceLevel(val displayName: String, val range: ClosedFloatingPointRange<Double>) {
    HIGH("High Confidence", 0.7..1.0),
    MODERATE("Moderate Confidence", 0.4..0.7),
    LOW("Low Confidence", 0.0..0.4);
    
    companion object {
        fun fromConfidence(confidence: Double): PatternConfidenceLevel {
            return when {
                confidence >= 0.7 -> HIGH
                confidence >= 0.4 -> MODERATE
                else -> LOW
            }
        }
    }
}

/**
 * Utility extensions for working with patterns
 */
fun List<SymptomPattern>.filterByConfidence(minConfidence: Double): List<SymptomPattern> {
    return this.filter { it.confidence >= minConfidence }
}

fun List<SymptomPattern>.groupByType(): Map<PatternType, List<SymptomPattern>> {
    return this.groupBy { it.patternType }
}

fun List<SymptomPattern>.groupBySymptom(): Map<String, List<SymptomPattern>> {
    return this.groupBy { it.symptomType }
}

fun List<SymptomPattern>.sortByConfidence(): List<SymptomPattern> {
    return this.sortedByDescending { it.confidence }
}

fun List<SymptomPattern>.getHighestConfidencePattern(): SymptomPattern? {
    return this.maxByOrNull { it.confidence }
}

fun List<SymptomPattern>.getMostFrequentPatternType(): PatternType? {
    return this.groupBy { it.patternType }
        .maxByOrNull { it.value.size }
        ?.key
}