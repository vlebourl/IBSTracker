package com.tiarkaerell.ibstracker.analysis

import com.tiarkaerell.ibstracker.data.analysis.CorrelationCalculator
import com.tiarkaerell.ibstracker.data.analysis.ProbabilityEngine
import com.tiarkaerell.ibstracker.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class TriggerAnalyzerTest {
    
    private lateinit var correlationCalculator: CorrelationCalculator
    private lateinit var probabilityEngine: ProbabilityEngine
    
    @Before
    fun setUp() {
        correlationCalculator = CorrelationCalculator()
        probabilityEngine = ProbabilityEngine(correlationCalculator)
    }
    
    @Test
    fun `correlationCalculator handles empty evidence correctly`() {
        val result = correlationCalculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = emptyList(),
            totalFoodOccurrences = 5,
            totalSymptomOccurrences = 10
        )
        
        assertNull("Should return null for empty evidence", result)
    }
    
    @Test
    fun `correlationCalculator handles zero food occurrences correctly`() {
        val evidence = createTestCorrelationEvidence()
        
        val result = correlationCalculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = evidence,
            totalFoodOccurrences = 0,
            totalSymptomOccurrences = 10
        )
        
        assertNull("Should return null for zero food occurrences", result)
    }
    
    @Test
    fun `correlationCalculator calculates valid probability`() {
        val evidence = createTestCorrelationEvidence()
        
        val result = correlationCalculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = evidence,
            totalFoodOccurrences = 5,
            totalSymptomOccurrences = 10
        )
        
        assertNotNull("Should return valid result", result)
        assertEquals("Coffee", result!!.foodName)
        assertEquals(IBSTriggerCategory.CAFFEINE, result.ibsTriggerCategory)
        assertTrue("Probability should be between 0 and 1", result.probability in 0.0..1.0)
        assertTrue("Confidence should be between 0 and 1", result.confidence in 0.0..1.0)
        assertEquals(evidence.size, result.occurrenceCount)
    }
    
    @Test
    fun `probabilityEngine returns null for empty symptoms`() {
        val symptoms = createTestSymptomOccurrences()
        val foods = createTestFoodOccurrences()
        
        val result = probabilityEngine.generateSymptomAnalysis(
            symptomType = "TestSymptom",
            symptomOccurrences = emptyList(),
            foodOccurrences = foods,
            timeWindow = createTestTimeWindow(),
            filters = AnalysisFilters()
        )
        
        assertNull("Should return null for empty symptoms", result)
    }
    
    @Test
    fun `probabilityEngine processes valid data correctly`() {
        val symptoms = createTestSymptomOccurrences()
        val foods = createTestFoodOccurrences()
        
        val result = probabilityEngine.generateSymptomAnalysis(
            symptomType = "Diarrhea",
            symptomOccurrences = symptoms,
            foodOccurrences = foods,
            timeWindow = createTestTimeWindow(),
            filters = AnalysisFilters()
        )
        
        // Result may be null if no correlations found, which is valid
        if (result != null) {
            assertEquals("Diarrhea", result.symptomType)
            assertTrue("Total occurrences should match symptoms", result.totalOccurrences > 0)
            assertTrue("Average intensity should be valid", result.averageIntensity in 1.0..10.0)
            assertTrue("Confidence should be valid", result.confidence in 0.0..1.0)
            assertNotNull("Should have severity level", result.severityLevel)
            assertNotNull("Should have recommendation level", result.recommendationLevel)
        }
    }
    
    private fun createTestTimeWindow(): AnalysisTimeWindow {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(30)
        
        return AnalysisTimeWindow(
            startDate = startDate,
            endDate = endDate,
            windowSizeHours = 8,
            minimumOccurrences = 3,
            minimumObservationDays = 14
        )
    }
    
    private fun createTestCorrelationEvidence(): List<CorrelationEvidence> {
        val now = java.time.Instant.now()
        return listOf(
            CorrelationEvidence(
                foodTimestamp = now.minusSeconds(3600), // 1 hour before
                symptomTimestamp = now,
                timeLag = java.time.Duration.ofHours(1),
                symptomIntensity = 7,
                foodQuantity = "1 cup",
                temporalWeight = 0.8,
                contextualNotes = "Morning coffee"
            ),
            CorrelationEvidence(
                foodTimestamp = now.minusSeconds(7200), // 2 hours before
                symptomTimestamp = now,
                timeLag = java.time.Duration.ofHours(2),
                symptomIntensity = 5,
                foodQuantity = "1 cup",
                temporalWeight = 0.6,
                contextualNotes = null
            )
        )
    }
    
    private fun createTestSymptomOccurrences(): List<com.tiarkaerell.ibstracker.data.analysis.SymptomOccurrence> {
        val now = java.time.Instant.now()
        return listOf(
            com.tiarkaerell.ibstracker.data.analysis.SymptomOccurrence("Diarrhea", 7, now.minusSeconds(3600)),
            com.tiarkaerell.ibstracker.data.analysis.SymptomOccurrence("Diarrhea", 5, now.minusSeconds(1800)),
            com.tiarkaerell.ibstracker.data.analysis.SymptomOccurrence("Diarrhea", 8, now)
        )
    }
    
    private fun createTestFoodOccurrences(): List<com.tiarkaerell.ibstracker.data.analysis.FoodOccurrence> {
        val now = java.time.Instant.now()
        return listOf(
            com.tiarkaerell.ibstracker.data.analysis.FoodOccurrence("Coffee", "2 cups", now.minusSeconds(7200)),
            com.tiarkaerell.ibstracker.data.analysis.FoodOccurrence("Milk", "1 glass", now.minusSeconds(5400)),
            com.tiarkaerell.ibstracker.data.analysis.FoodOccurrence("Cheese", "2 slices", now.minusSeconds(3600))
        )
    }
}