package com.tiarkaerell.ibstracker.data.repository

import com.tiarkaerell.ibstracker.data.analysis.TriggerAnalyzer
import com.tiarkaerell.ibstracker.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

class AnalysisRepositoryImpl(
    private val triggerAnalyzer: TriggerAnalyzer
) : AnalysisRepository {
    
    private val _currentAnalysisResult = MutableStateFlow<AnalysisResult?>(null)
    private val analysisCache = mutableMapOf<String, CachedAnalysis>()
    private val incrementalCacheData = mutableMapOf<String, IncrementalCacheData>()
    
    override suspend fun generateAnalysis(
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): AnalysisResult {
        val cacheKey = generateCacheKey(timeWindow, filters)
        
        // Check for incremental cache update opportunity
        val incrementalData = incrementalCacheData[cacheKey]
        if (incrementalData != null && canUseIncrementalUpdate(incrementalData, timeWindow)) {
            val incrementalResult = performIncrementalUpdate(incrementalData, timeWindow, filters)
            if (incrementalResult != null) {
                analysisCache[cacheKey] = CachedAnalysis(
                    result = incrementalResult,
                    timestamp = Instant.now()
                )
                _currentAnalysisResult.value = incrementalResult
                return incrementalResult
            }
        }
        
        val cachedAnalysis = analysisCache[cacheKey]
        if (cachedAnalysis != null && !isCacheExpired(cachedAnalysis)) {
            _currentAnalysisResult.value = cachedAnalysis.result
            return cachedAnalysis.result
        }
        
        val result = triggerAnalyzer.generateAnalysisResult(timeWindow, filters)
        
        // Enhance the result with detailed correlation explanations
        val enhancedResult = result.copy(
            symptomAnalyses = result.symptomAnalyses.map { analysis ->
                analysis.copy(
                    insights = analysis.insights + generateDetailedCorrelationExplanations(analysis),
                    triggerProbabilities = analysis.triggerProbabilities.map { trigger ->
                        trigger.copy(
                            supportingEvidence = trigger.supportingEvidence.map { evidence ->
                                evidence.copy(
                                    contextualNotes = enhanceEvidenceContext(evidence, trigger)
                                )
                            }
                        )
                    }
                )
            }
        )
        
        // Store both full analysis and incremental cache data
        analysisCache[cacheKey] = CachedAnalysis(
            result = enhancedResult,
            timestamp = Instant.now()
        )
        
        incrementalCacheData[cacheKey] = IncrementalCacheData(
            lastFullAnalysisTimestamp = Instant.now(),
            cachedCorrelations = result.symptomAnalyses.flatMap { it.triggerProbabilities },
            dataHash = calculateDataHash(timeWindow, filters)
        )
        
        _currentAnalysisResult.value = enhancedResult
        return enhancedResult
    }
    
    override suspend fun getCachedAnalysis(
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): AnalysisResult? {
        val cacheKey = generateCacheKey(timeWindow, filters)
        val cachedAnalysis = analysisCache[cacheKey]
        
        return if (cachedAnalysis != null && !isCacheExpired(cachedAnalysis)) {
            cachedAnalysis.result
        } else {
            null
        }
    }
    
    override suspend fun invalidateCache(since: Instant) {
        val keysToRemove = analysisCache.filterValues { cached ->
            cached.timestamp.isBefore(since)
        }.keys
        
        keysToRemove.forEach { key ->
            analysisCache.remove(key)
        }
        
        if (keysToRemove.isNotEmpty()) {
            _currentAnalysisResult.value = null
        }
    }
    
    override fun observeAnalysisResults(): Flow<AnalysisResult?> {
        return _currentAnalysisResult.asStateFlow()
    }
    
    private fun generateCacheKey(timeWindow: AnalysisTimeWindow, filters: AnalysisFilters): String {
        return "${timeWindow.startDate}_${timeWindow.endDate}_${timeWindow.windowSizeHours}_" +
               "${timeWindow.minimumOccurrences}_${timeWindow.minimumObservationDays}_" +
               "${filters.severityThreshold}_${filters.symptomTypes.sorted()}_" +
               "${filters.foodCategories.sorted()}_${filters.excludeFoods.sorted()}_" +
               "${filters.minimumConfidence}_${filters.showLowOccurrenceCorrelations}"
    }
    
    private fun isCacheExpired(cachedAnalysis: CachedAnalysis): Boolean {
        val cacheMaxAge = java.time.Duration.ofMinutes(30)
        return cachedAnalysis.timestamp.plus(cacheMaxAge).isBefore(Instant.now())
    }
    
    private fun canUseIncrementalUpdate(
        incrementalData: IncrementalCacheData,
        timeWindow: AnalysisTimeWindow
    ): Boolean {
        val timeSinceLastFullAnalysis = java.time.Duration.between(
            incrementalData.lastFullAnalysisTimestamp,
            Instant.now()
        )
        
        // Use incremental updates if less than 2 hours since last full analysis
        return timeSinceLastFullAnalysis.toHours() < 2
    }
    
    private suspend fun performIncrementalUpdate(
        incrementalData: IncrementalCacheData,
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): AnalysisResult? {
        // For incremental updates, we would fetch only new data since last analysis
        // This is a simplified implementation - in practice, you'd query for data
        // added since incrementalData.lastFullAnalysisTimestamp
        
        return try {
            // Placeholder for incremental update logic
            // In a real implementation, this would:
            // 1. Fetch only new symptoms/foods since last analysis
            // 2. Update correlations with new data
            // 3. Merge with existing cached correlations
            null // Return null to fall back to full analysis for now
        } catch (e: Exception) {
            null // Fall back to full analysis on error
        }
    }
    
    private fun calculateDataHash(timeWindow: AnalysisTimeWindow, filters: AnalysisFilters): String {
        return "${timeWindow.hashCode()}_${filters.hashCode()}_${Instant.now().epochSecond / 3600}"
    }
    
    private data class CachedAnalysis(
        val result: AnalysisResult,
        val timestamp: Instant
    )
    
    private data class IncrementalCacheData(
        val lastFullAnalysisTimestamp: Instant,
        val cachedCorrelations: List<TriggerProbability>,
        val dataHash: String
    )
    
    /**
     * Generate detailed correlation explanations for a symptom analysis
     */
    private fun generateDetailedCorrelationExplanations(analysis: SymptomAnalysis): List<String> {
        val explanations = mutableListOf<String>()
        
        // Overall pattern explanation
        if (analysis.triggerProbabilities.isNotEmpty()) {
            val highProbabilityTriggers = analysis.triggerProbabilities.filter { it.probability >= 0.7 }
            val moderateProbabilityTriggers = analysis.triggerProbabilities.filter { it.probability >= 0.4 && it.probability < 0.7 }
            
            when {
                highProbabilityTriggers.isNotEmpty() -> {
                    explanations.add("Strong correlations detected: ${highProbabilityTriggers.joinToString(", ") { "${it.foodName} (${it.probabilityPercentage}%)" }}")
                }
                moderateProbabilityTriggers.isNotEmpty() -> {
                    explanations.add("Moderate correlations found: ${moderateProbabilityTriggers.joinToString(", ") { "${it.foodName} (${it.probabilityPercentage}%)" }}")
                }
                else -> {
                    explanations.add("Weak correlations identified - patterns may emerge with more data")
                }
            }
        }
        
        // Confidence-based explanation
        when {
            analysis.confidence >= 0.8 -> explanations.add("High confidence analysis based on consistent symptom patterns and sufficient data points")
            analysis.confidence >= 0.5 -> explanations.add("Moderate confidence - patterns are emerging but could benefit from additional tracking")
            else -> explanations.add("Limited confidence due to insufficient data - continue tracking for more reliable patterns")
        }
        
        // Severity context
        when (analysis.severityLevel) {
            SeverityLevel.HIGH -> explanations.add("High severity symptoms (avg ${String.format("%.1f", analysis.averageIntensity)}/10) suggest significant triggers that may require dietary adjustments")
            SeverityLevel.MEDIUM -> explanations.add("Moderate severity symptoms (avg ${String.format("%.1f", analysis.averageIntensity)}/10) indicate manageable triggers that can be monitored")
            SeverityLevel.LOW -> explanations.add("Mild symptoms (avg ${String.format("%.1f", analysis.averageIntensity)}/10) suggest well-managed condition with minor trigger influences")
        }
        
        // Temporal insights
        if (analysis.triggerProbabilities.isNotEmpty()) {
            val avgTimeLag = analysis.triggerProbabilities.map { it.averageTimeLag.toHours() }.average()
            when {
                avgTimeLag <= 2 -> explanations.add("Symptoms typically appear within 2 hours of eating trigger foods")
                avgTimeLag <= 4 -> explanations.add("Delayed symptom onset (2-4 hours) suggests slower digestive processing")
                else -> explanations.add("Late symptom onset (4+ hours) may indicate complex digestive responses")
            }
        }
        
        // Recommendation guidance
        when (analysis.recommendationLevel) {
            RecommendationLevel.HIGH -> explanations.add("Recommendation: Consider avoiding identified high-probability triggers and consult healthcare provider")
            RecommendationLevel.MEDIUM -> explanations.add("Recommendation: Monitor moderate triggers closely and consider portion control")
            RecommendationLevel.LOW_CONFIDENCE -> explanations.add("Recommendation: Continue tracking - insufficient data for specific dietary recommendations")
            RecommendationLevel.HIDE -> explanations.add("Insufficient correlation data available for reliable recommendations")
        }
        
        return explanations
    }
    
    /**
     * Enhance evidence context with detailed explanations
     */
    private fun enhanceEvidenceContext(evidence: CorrelationEvidence, trigger: TriggerProbability): String {
        val originalNotes = evidence.contextualNotes ?: ""
        val timeLagHours = evidence.timeLag.toHours()
        val timeLagDescription = when {
            timeLagHours < 1 -> "within 1 hour"
            timeLagHours < 2 -> "within 2 hours"
            timeLagHours < 4 -> "within 2-4 hours"
            else -> "after 4+ hours"
        }
        
        val intensityDescription = when (evidence.symptomIntensity) {
            in 1..3 -> "mild"
            in 4..6 -> "moderate"
            in 7..8 -> "severe"
            in 9..10 -> "very severe"
            else -> "unknown intensity"
        }
        
        val correlationStrength = when {
            trigger.probability >= 0.8 -> "very strong"
            trigger.probability >= 0.6 -> "strong"
            trigger.probability >= 0.4 -> "moderate"
            else -> "weak"
        }
        
        val enhancedContext = buildString {
            if (originalNotes.isNotBlank()) {
                append(originalNotes)
                append(" | ")
            }
            append("Correlation: $correlationStrength association, ")
            append("$intensityDescription symptoms occurred $timeLagDescription after consuming ${evidence.foodQuantity}")
            
            if (evidence.temporalWeight >= 0.8) {
                append(" (rapid onset pattern)")
            } else if (evidence.temporalWeight <= 0.3) {
                append(" (delayed reaction pattern)")
            }
        }
        
        return enhancedContext
    }
}