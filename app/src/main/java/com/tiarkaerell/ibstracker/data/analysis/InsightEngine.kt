package com.tiarkaerell.ibstracker.data.analysis

import com.tiarkaerell.ibstracker.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Engine for generating plain-language insights and recommendations 
 * from symptom analysis data
 */
class InsightEngine {
    
    /**
     * Generates comprehensive plain-language summary for an analysis result
     */
    fun generateAnalysisSummary(result: AnalysisResult): List<String> {
        val insights = mutableListOf<String>()
        
        // Overall analysis summary
        insights.add(generateOverallSummary(result))
        
        // Key findings
        if (result.symptomAnalyses.isNotEmpty()) {
            insights.addAll(generateKeyFindings(result.symptomAnalyses))
        }
        
        // Reliability assessment
        insights.add(generateReliabilityAssessment(result))
        
        // Data sufficiency insights
        insights.addAll(generateDataSufficiencyInsights(result))
        
        return insights
    }
    
    /**
     * Generates actionable recommendations based on trigger probabilities
     */
    fun generateRecommendations(analyses: List<SymptomAnalysis>): List<String> {
        val recommendations = mutableListOf<String>()
        
        val highConfidenceTriggers = analyses.flatMap { it.triggerProbabilities }
            .filter { it.confidence >= 0.7 && it.probability >= 0.6 }
            .sortedByDescending { it.probability }
        
        val moderateConfidenceTriggers = analyses.flatMap { it.triggerProbabilities }
            .filter { it.confidence >= 0.5 && it.probability >= 0.4 && it.confidence < 0.7 }
            .sortedByDescending { it.probability }
        
        // High confidence recommendations
        if (highConfidenceTriggers.isNotEmpty()) {
            recommendations.add("Strong recommendations based on your data:")
            highConfidenceTriggers.take(3).forEach { trigger ->
                recommendations.add("• Consider avoiding ${trigger.foodName} (${trigger.probabilityPercentage}% trigger probability)")
            }
        }
        
        // Moderate confidence recommendations
        if (moderateConfidenceTriggers.isNotEmpty()) {
            recommendations.add("Monitor these potential triggers closely:")
            moderateConfidenceTriggers.take(3).forEach { trigger ->
                recommendations.add("• Watch portions of ${trigger.foodName} (${trigger.probabilityPercentage}% trigger probability)")
            }
        }
        
        // General recommendations
        recommendations.addAll(generateGeneralRecommendations(analyses))
        
        return recommendations
    }
    
    /**
     * Generates insights about symptom patterns and trends
     */
    fun generatePatternInsights(analyses: List<SymptomAnalysis>): List<String> {
        val insights = mutableListOf<String>()
        
        // Severity patterns
        val severityDistribution = analyses.groupBy { it.severityLevel }
        insights.addAll(generateSeverityPatternInsights(severityDistribution))
        
        // Trigger category patterns
        val categoryPatterns = analyzeTriggerCategories(analyses)
        insights.addAll(generateCategoryPatternInsights(categoryPatterns))
        
        // Timing patterns
        insights.addAll(generateTimingPatternInsights(analyses))
        
        return insights
    }
    
    /**
     * Generates personalized insights based on user's specific data patterns
     */
    fun generatePersonalizedInsights(analyses: List<SymptomAnalysis>, timeWindow: AnalysisTimeWindow): List<String> {
        val insights = mutableListOf<String>()
        
        // Most problematic symptoms
        val primarySymptoms = analyses.filter { it.severityLevel == SeverityLevel.HIGH }
        if (primarySymptoms.isNotEmpty()) {
            insights.add("Your primary concern appears to be ${primarySymptoms.first().symptomType.lowercase()}, " +
                        "occurring ${primarySymptoms.first().totalOccurrences} times in the past ${timeWindow.totalDays()} days.")
        }
        
        // Progress indicators
        insights.addAll(generateProgressInsights(analyses, timeWindow))
        
        // Lifestyle insights
        insights.addAll(generateLifestyleInsights(analyses))
        
        return insights
    }
    
    private fun generateOverallSummary(result: AnalysisResult): String {
        val symptomCount = result.symptomAnalyses.size
        val highConfidenceCount = result.symptomAnalyses.count { it.confidence >= 0.7 }
        val reliabilityPercent = (result.reliabilityScore * 100).roundToInt()
        
        return when {
            symptomCount == 0 -> "No significant symptom patterns detected in your tracking data."
            highConfidenceCount >= 2 -> "Analysis found $symptomCount symptom patterns with $highConfidenceCount showing high confidence correlations ($reliabilityPercent% reliability)."
            highConfidenceCount == 1 -> "Analysis identified $symptomCount symptom patterns with one showing strong food correlations ($reliabilityPercent% reliability)."
            else -> "Analysis detected $symptomCount symptom patterns. Continue tracking for stronger correlations ($reliabilityPercent% reliability)."
        }
    }
    
    private fun generateKeyFindings(analyses: List<SymptomAnalysis>): List<String> {
        val findings = mutableListOf<String>()
        
        // Most significant finding
        val topAnalysis = analyses.maxByOrNull { it.confidence * (it.triggerProbabilities.firstOrNull()?.probability ?: 0.0) }
        topAnalysis?.let { analysis ->
            val topTrigger = analysis.triggerProbabilities.firstOrNull()
            if (topTrigger != null && analysis.confidence >= 0.6) {
                findings.add("Key finding: ${analysis.symptomType} shows ${topTrigger.probabilityPercentage}% correlation with ${topTrigger.foodName}.")
            }
        }
        
        // Category trends
        val categoryTrends = analyses.flatMap { it.triggerProbabilities }
            .groupBy { it.ibsTriggerCategory }
            .mapValues { (_, triggers) -> triggers.map { it.probability }.average() }
            .filter { it.value >= 0.4 }
            .toList()
            .sortedByDescending { it.second }
        
        if (categoryTrends.isNotEmpty()) {
            val topCategory = categoryTrends.first()
            findings.add("Food pattern: ${topCategory.first.displayName} foods show the strongest correlations overall.")
        }
        
        return findings
    }
    
    private fun generateReliabilityAssessment(result: AnalysisResult): String {
        return when {
            result.reliabilityScore >= 0.8 -> "Analysis reliability is high - patterns are well-established with sufficient data."
            result.reliabilityScore >= 0.6 -> "Analysis reliability is good - patterns are emerging with adequate tracking data."
            result.reliabilityScore >= 0.4 -> "Analysis reliability is moderate - continue tracking for stronger patterns."
            else -> "Analysis reliability is limited due to insufficient tracking data or unclear patterns."
        }
    }
    
    private fun generateDataSufficiencyInsights(result: AnalysisResult): List<String> {
        val insights = mutableListOf<String>()
        
        when {
            result.observationPeriodDays < 14 -> {
                insights.add("Recommendation: Track for at least 2 weeks for more reliable patterns.")
            }
            result.totalSymptomOccurrences < 10 -> {
                insights.add("Tip: Log more symptom occurrences to improve correlation accuracy.")
            }
            result.totalFoodEntries < 20 -> {
                insights.add("Tip: Track more diverse foods to identify potential triggers.")
            }
            else -> {
                insights.add("Data coverage: Excellent tracking consistency supports reliable analysis.")
            }
        }
        
        return insights
    }
    
    private fun generateGeneralRecommendations(analyses: List<SymptomAnalysis>): List<String> {
        val recommendations = mutableListOf<String>()
        
        // If no strong patterns
        if (analyses.none { it.confidence >= 0.7 }) {
            recommendations.add("General advice:")
            recommendations.add("• Continue consistent food and symptom tracking")
            recommendations.add("• Consider consulting a healthcare provider for personalized guidance")
            recommendations.add("• Try keeping portion sizes moderate for suspected triggers")
        }
        
        // If severe symptoms present
        if (analyses.any { it.severityLevel == SeverityLevel.HIGH }) {
            recommendations.add("Important: For severe symptoms, consult with a healthcare professional for proper evaluation.")
        }
        
        return recommendations
    }
    
    private fun generateSeverityPatternInsights(severityDistribution: Map<SeverityLevel, List<SymptomAnalysis>>): List<String> {
        val insights = mutableListOf<String>()
        
        val highSeverity = severityDistribution[SeverityLevel.HIGH]?.size ?: 0
        val moderateSeverity = severityDistribution[SeverityLevel.MEDIUM]?.size ?: 0
        val lowSeverity = severityDistribution[SeverityLevel.LOW]?.size ?: 0
        
        when {
            highSeverity > moderateSeverity + lowSeverity -> {
                insights.add("Pattern: Most of your symptoms are high severity, suggesting significant trigger foods.")
            }
            moderateSeverity > highSeverity + lowSeverity -> {
                insights.add("Pattern: Symptoms are primarily moderate, indicating manageable triggers.")
            }
            lowSeverity > highSeverity + moderateSeverity -> {
                insights.add("Pattern: Most symptoms are mild, suggesting good overall trigger management.")
            }
        }
        
        return insights
    }
    
    private fun analyzeTriggerCategories(analyses: List<SymptomAnalysis>): Map<IBSTriggerCategory, Double> {
        return analyses.flatMap { it.triggerProbabilities }
            .groupBy { it.ibsTriggerCategory }
            .mapValues { (_, triggers) -> triggers.map { it.probability }.average() }
    }
    
    private fun generateCategoryPatternInsights(categoryPatterns: Map<IBSTriggerCategory, Double>): List<String> {
        val insights = mutableListOf<String>()
        
        val topCategories = categoryPatterns.toList()
            .sortedByDescending { it.second }
            .take(2)
            .filter { it.second >= 0.3 }
        
        if (topCategories.isNotEmpty()) {
            insights.add("Category insight: ${topCategories.first().first.displayName} foods are your primary trigger category.")
            
            if (topCategories.size > 1) {
                insights.add("Secondary pattern: ${topCategories[1].first.displayName} foods also show correlations.")
            }
        }
        
        return insights
    }
    
    private fun generateTimingPatternInsights(analyses: List<SymptomAnalysis>): List<String> {
        val insights = mutableListOf<String>()
        
        val avgTimeLags = analyses.flatMap { it.triggerProbabilities }
            .map { it.averageTimeLag.toHours() }
        
        if (avgTimeLags.isNotEmpty()) {
            val avgHours = avgTimeLags.average()
            
            when {
                avgHours <= 1 -> insights.add("Timing pattern: Symptoms typically appear within 1 hour of eating trigger foods.")
                avgHours <= 3 -> insights.add("Timing pattern: Symptoms usually develop within 2-3 hours after meals.")
                avgHours <= 6 -> insights.add("Timing pattern: Symptoms tend to appear 3-6 hours after eating.")
                else -> insights.add("Timing pattern: Symptoms show delayed onset, appearing 6+ hours after eating.")
            }
        }
        
        return insights
    }
    
    private fun generateProgressInsights(analyses: List<SymptomAnalysis>, timeWindow: AnalysisTimeWindow): List<String> {
        val insights = mutableListOf<String>()
        
        val avgOccurrences = analyses.map { it.totalOccurrences }.average()
        val dailyRate = avgOccurrences / timeWindow.totalDays()
        
        when {
            dailyRate <= 0.3 -> insights.add("Progress: Low symptom frequency suggests good trigger management.")
            dailyRate <= 0.7 -> insights.add("Progress: Moderate symptom frequency indicates room for improvement.")
            else -> insights.add("Progress: Frequent symptoms suggest need for more targeted trigger identification.")
        }
        
        return insights
    }
    
    private fun generateLifestyleInsights(analyses: List<SymptomAnalysis>): List<String> {
        val insights = mutableListOf<String>()
        
        // High FODMAP insight
        val fodmapTriggers = analyses.flatMap { it.triggerProbabilities }
            .filter { it.ibsTriggerCategory == IBSTriggerCategory.FODMAP_HIGH }
        
        if (fodmapTriggers.isNotEmpty() && fodmapTriggers.any { it.probability >= 0.5 }) {
            insights.add("Lifestyle tip: Consider exploring a low-FODMAP diet with healthcare guidance.")
        }
        
        // Dairy insight
        val dairyTriggers = analyses.flatMap { it.triggerProbabilities }
            .filter { it.ibsTriggerCategory == IBSTriggerCategory.DAIRY }
        
        if (dairyTriggers.isNotEmpty() && dairyTriggers.any { it.probability >= 0.5 }) {
            insights.add("Lifestyle tip: Dairy alternatives might help reduce your symptoms.")
        }
        
        return insights
    }
}