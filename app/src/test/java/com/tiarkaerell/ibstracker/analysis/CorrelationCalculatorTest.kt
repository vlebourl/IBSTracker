package com.tiarkaerell.ibstracker.analysis

import com.tiarkaerell.ibstracker.data.analysis.CorrelationCalculator
import com.tiarkaerell.ibstracker.data.model.CorrelationEvidence
import com.tiarkaerell.ibstracker.data.model.IBSTriggerCategory
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant

class CorrelationCalculatorTest {
    
    private lateinit var calculator: CorrelationCalculator
    
    @Before
    fun setUp() {
        calculator = CorrelationCalculator()
    }
    
    @Test
    fun `calculateTriggerProbability returns null for empty evidence`() {
        val result = calculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = emptyList(),
            totalFoodOccurrences = 5,
            totalSymptomOccurrences = 10
        )
        
        assertNull(result)
    }
    
    @Test
    fun `calculateTriggerProbability returns null for zero food occurrences`() {
        val evidence = createTestEvidence()
        
        val result = calculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = evidence,
            totalFoodOccurrences = 0,
            totalSymptomOccurrences = 10
        )
        
        assertNull(result)
    }
    
    @Test
    fun `calculateTriggerProbability returns valid result for normal case`() {
        val evidence = createTestEvidence()
        
        val result = calculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = evidence,
            totalFoodOccurrences = 5,
            totalSymptomOccurrences = 10
        )
        
        assertNotNull(result)
        assertEquals("Coffee", result!!.foodName)
        assertEquals(IBSTriggerCategory.CAFFEINE, result.ibsTriggerCategory)
        assertEquals(evidence.size, result.occurrenceCount)
        assertEquals((result.probability * 100).toInt(), result.probabilityPercentage)
        assertTrue(result.probability in 0.0..1.0)
        assertTrue(result.confidence in 0.0..1.0)
    }
    
    @Test
    fun `probability calculation respects weighted formula`() {
        val evidence = createTestEvidence()
        
        val result = calculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = evidence,
            totalFoodOccurrences = 5,
            totalSymptomOccurrences = 10
        )
        
        assertNotNull(result)
        
        val expectedBaseline = IBSTriggerCategory.CAFFEINE.baselineProbability
        assertTrue("Baseline score should match category", result!!.baselineScore == expectedBaseline)
        assertTrue("Temporal score should be calculated", result.temporalScore >= 0.0)
        assertTrue("Frequency score should be calculated", result.frequencyScore >= 0.0)
    }
    
    @Test
    fun `confidence increases with more evidence`() {
        val smallEvidence = createTestEvidence().take(1)
        val largeEvidence = createTestEvidence()
        
        val smallResult = calculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = smallEvidence,
            totalFoodOccurrences = 5,
            totalSymptomOccurrences = 10
        )
        
        val largeResult = calculator.calculateTriggerProbability(
            foodName = "Coffee",
            ibsTriggerCategory = IBSTriggerCategory.CAFFEINE,
            correlationEvidence = largeEvidence,
            totalFoodOccurrences = 5,
            totalSymptomOccurrences = 10
        )
        
        assertNotNull(smallResult)
        assertNotNull(largeResult)
        assertTrue("More evidence should increase confidence", 
                  largeResult!!.confidence >= smallResult!!.confidence)
    }
    
    private fun createTestEvidence(): List<CorrelationEvidence> {
        val now = Instant.now()
        return listOf(
            CorrelationEvidence(
                foodTimestamp = now.minusSeconds(3600), // 1 hour before
                symptomTimestamp = now,
                timeLag = Duration.ofHours(1),
                symptomIntensity = 7,
                foodQuantity = "1 cup",
                temporalWeight = 0.8,
                contextualNotes = "Morning coffee"
            ),
            CorrelationEvidence(
                foodTimestamp = now.minusSeconds(7200), // 2 hours before
                symptomTimestamp = now,
                timeLag = Duration.ofHours(2),
                symptomIntensity = 5,
                foodQuantity = "1 cup",
                temporalWeight = 0.6,
                contextualNotes = null
            ),
            CorrelationEvidence(
                foodTimestamp = now.minusSeconds(1800), // 30 minutes before
                symptomTimestamp = now,
                timeLag = Duration.ofMinutes(30),
                symptomIntensity = 8,
                foodQuantity = "2 cups",
                temporalWeight = 0.9,
                contextualNotes = "Extra strong"
            )
        )
    }
}