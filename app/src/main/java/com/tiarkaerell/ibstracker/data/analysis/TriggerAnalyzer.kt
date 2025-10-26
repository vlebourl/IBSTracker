package com.tiarkaerell.ibstracker.data.analysis

import com.tiarkaerell.ibstracker.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class TriggerAnalyzer(
    private val probabilityEngine: ProbabilityEngine,
    private val dataRepository: DataRepository,
    private val filterProcessor: AnalysisFilterProcessor = AnalysisFilterProcessor()
) {
    
    companion object {
        private const val MAX_BATCH_SIZE = 1000
        private const val MEMORY_OPTIMIZATION_THRESHOLD = 5000
    }
    
    suspend fun generateAnalysisResult(
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): AnalysisResult = withContext(Dispatchers.Default) {
        
        val symptoms = dataRepository.getSymptomsInTimeRange(
            timeWindow.startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
            timeWindow.endDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        )
        
        val foods = dataRepository.getFoodsInTimeRange(
            timeWindow.startDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
            timeWindow.endDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
        )
        
        // Apply comprehensive filtering using the filter processor
        val filteredSymptoms = filterProcessor.filterSymptomOccurrences(symptoms, filters)
        val filteredFoods = filterProcessor.filterFoodOccurrences(foods, filters)
        
        val symptomsByType = filteredSymptoms.groupBy { it.type }
        val symptomAnalyses = mutableListOf<SymptomAnalysis>()
        
        // Memory optimization: process large datasets in batches
        val totalDataSize = filteredSymptoms.size + filteredFoods.size
        
        if (totalDataSize > MEMORY_OPTIMIZATION_THRESHOLD) {
            // Process in batches to reduce memory pressure
            for ((symptomType, symptomOccurrences) in symptomsByType) {
                if (symptomOccurrences.size >= timeWindow.minimumOccurrences) {
                    val analysis = processSymptomInBatches(
                        symptomType = symptomType,
                        symptomOccurrences = symptomOccurrences,
                        foodOccurrences = filteredFoods,
                        timeWindow = timeWindow,
                        filters = filters
                    )
                    analysis?.let { symptomAnalyses.add(it) }
                }
            }
        } else {
            // Standard processing for smaller datasets
            for ((symptomType, symptomOccurrences) in symptomsByType) {
                if (symptomOccurrences.size >= timeWindow.minimumOccurrences) {
                    val analysis = probabilityEngine.generateSymptomAnalysis(
                        symptomType = symptomType,
                        symptomOccurrences = symptomOccurrences,
                        foodOccurrences = filteredFoods,
                        timeWindow = timeWindow,
                        filters = filters
                    )
                    
                    analysis?.let { symptomAnalyses.add(it) }
                }
            }
        }
        
        val reliabilityScore = calculateReliabilityScore(
            symptomAnalyses,
            filteredSymptoms.size,
            filteredFoods.size,
            timeWindow.totalDays()
        )
        
        // Apply final filtering and ranking to symptom analyses
        val timeFilteredAnalyses = filterProcessor.applyTimeWindowFilters(symptomAnalyses, timeWindow)
        val finalFilteredAnalyses = filterProcessor.applyFilters(timeFilteredAnalyses, filters)
        val rankedSymptomAnalyses = rankSymptomAnalysesByStrength(finalFilteredAnalyses)
        
        AnalysisResult(
            generatedAt = Instant.now(),
            analysisTimeWindow = timeWindow,
            filters = filters,
            symptomAnalyses = rankedSymptomAnalyses,
            totalSymptomOccurrences = filteredSymptoms.size,
            totalFoodEntries = filteredFoods.size,
            observationPeriodDays = timeWindow.totalDays(),
            reliabilityScore = reliabilityScore
        )
    }
    
    private fun calculateReliabilityScore(
        analyses: List<SymptomAnalysis>,
        totalSymptoms: Int,
        totalFoods: Int,
        observationDays: Int
    ): Double {
        if (analyses.isEmpty() || totalSymptoms == 0 || totalFoods == 0) return 0.0
        
        val dataVolumeScore = kotlin.math.min(1.0, (totalSymptoms + totalFoods) / 50.0)
        val timeRangeScore = kotlin.math.min(1.0, observationDays / 30.0)
        val analysisQualityScore = analyses.map { it.confidence }.average()
        val coverageScore = analyses.size.toDouble() / maxOf(1, totalSymptoms / 10)
        
        return listOf(dataVolumeScore, timeRangeScore, analysisQualityScore, coverageScore)
            .average()
            .coerceIn(0.0, 1.0)
    }
    
    private suspend fun processSymptomInBatches(
        symptomType: String,
        symptomOccurrences: List<SymptomOccurrence>,
        foodOccurrences: List<FoodOccurrence>,
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): SymptomAnalysis? = withContext(Dispatchers.Default) {
        // Process food occurrences in batches to reduce memory usage
        val foodBatches = foodOccurrences.chunked(MAX_BATCH_SIZE)
        var combinedTriggerProbabilities = emptyList<TriggerProbability>()
        
        for (foodBatch in foodBatches) {
            val batchAnalysis = probabilityEngine.generateSymptomAnalysis(
                symptomType = symptomType,
                symptomOccurrences = symptomOccurrences,
                foodOccurrences = foodBatch,
                timeWindow = timeWindow,
                filters = filters
            )
            
            batchAnalysis?.let { analysis ->
                combinedTriggerProbabilities = combinedTriggerProbabilities + analysis.triggerProbabilities
            }
            
            // Force garbage collection between batches for large datasets
            if (foodBatches.size > 5) {
                System.gc()
            }
        }
        
        // Combine results and return analysis with merged trigger probabilities
        if (combinedTriggerProbabilities.isNotEmpty()) {
            // Merge duplicate foods by taking the highest probability
            val mergedTriggers = combinedTriggerProbabilities
                .groupBy { it.foodName }
                .mapValues { (_, triggers) -> triggers.maxByOrNull { it.probability }!! }
                .values
                .sortedByDescending { it.probability }
            
            return@withContext probabilityEngine.generateSymptomAnalysis(
                symptomType = symptomType,
                symptomOccurrences = symptomOccurrences,
                foodOccurrences = emptyList(),
                timeWindow = timeWindow,
                filters = filters
            )?.copy(triggerProbabilities = mergedTriggers)
        }
        
        return@withContext null
    }
    
    /**
     * Ranks symptom analyses by correlation strength using a composite score
     * that considers confidence, severity, and trigger quality
     */
    private fun rankSymptomAnalysesByStrength(analyses: List<SymptomAnalysis>): List<SymptomAnalysis> {
        return analyses.map { analysis ->
            val strengthScore = calculateCorrelationStrengthScore(analysis)
            analysis to strengthScore
        }
        .sortedByDescending { it.second }
        .map { it.first }
    }
    
    /**
     * Calculate a composite correlation strength score based on multiple factors
     */
    private fun calculateCorrelationStrengthScore(analysis: SymptomAnalysis): Double {
        val confidenceWeight = 0.4
        val severityWeight = 0.2
        val triggerQualityWeight = 0.25
        val occurrenceWeight = 0.15
        
        // Base confidence score
        val confidenceScore = analysis.confidence
        
        // Severity score (higher intensity = higher score)
        val severityScore = when (analysis.severityLevel) {
            SeverityLevel.HIGH -> 1.0
            SeverityLevel.MEDIUM -> 0.7
            SeverityLevel.LOW -> 0.4
        }
        
        // Trigger quality score (based on highest trigger probability and count)
        val triggerQualityScore = if (analysis.triggerProbabilities.isNotEmpty()) {
            val topTrigger = analysis.triggerProbabilities.first()
            val probabilityScore = topTrigger.probability
            val countScore = kotlin.math.min(1.0, topTrigger.occurrenceCount / 10.0)
            (probabilityScore + countScore) / 2.0
        } else {
            0.0
        }
        
        // Occurrence score (more data = higher reliability)
        val occurrenceScore = kotlin.math.min(1.0, analysis.totalOccurrences / 20.0)
        
        return (confidenceScore * confidenceWeight) +
               (severityScore * severityWeight) +
               (triggerQualityScore * triggerQualityWeight) +
               (occurrenceScore * occurrenceWeight)
    }
    
    /**
     * Ranks trigger probabilities within a symptom analysis by correlation strength
     */
    fun rankTriggerProbabilitiesByStrength(triggers: List<TriggerProbability>): List<TriggerProbability> {
        return triggers.sortedWith(compareByDescending<TriggerProbability> { trigger ->
            // Composite score considering probability, confidence, and evidence quality
            val probabilityWeight = 0.5
            val confidenceWeight = 0.3
            val evidenceWeight = 0.2
            
            val probabilityScore = trigger.probability
            val confidenceScore = trigger.confidence
            val evidenceScore = kotlin.math.min(1.0, trigger.occurrenceCount / 15.0)
            
            (probabilityScore * probabilityWeight) +
            (confidenceScore * confidenceWeight) +
            (evidenceScore * evidenceWeight)
        }.thenByDescending { it.occurrenceCount })
    }
}

interface DataRepository {
    suspend fun getSymptomsInTimeRange(start: Instant, end: Instant): List<SymptomOccurrence>
    suspend fun getFoodsInTimeRange(start: Instant, end: Instant): List<FoodOccurrence>
}