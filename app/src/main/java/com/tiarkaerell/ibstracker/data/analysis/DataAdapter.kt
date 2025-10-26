package com.tiarkaerell.ibstracker.data.analysis

import com.tiarkaerell.ibstracker.data.repository.DataRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.util.Date

class DataAdapter(
    private val dataRepository: DataRepository
) : com.tiarkaerell.ibstracker.data.analysis.DataRepository {
    
    override suspend fun getSymptomsInTimeRange(start: Instant, end: Instant): List<SymptomOccurrence> {
        val startDate = Date.from(start)
        val endDate = Date.from(end)
        
        val symptoms = dataRepository.getAllSymptoms().first()
        
        return symptoms
            .filter { symptom ->
                symptom.date.time >= startDate.time && symptom.date.time <= endDate.time
            }
            .map { symptom ->
                SymptomOccurrence(
                    type = symptom.name,
                    intensity = symptom.intensity,
                    timestamp = symptom.date.toInstant(),
                    notes = null
                )
            }
    }
    
    override suspend fun getFoodsInTimeRange(start: Instant, end: Instant): List<FoodOccurrence> {
        val startDate = Date.from(start)
        val endDate = Date.from(end)
        
        val foods = dataRepository.getFoodItemsByDateRange(startDate, endDate).first()
        
        return foods.map { foodItem ->
            FoodOccurrence(
                name = foodItem.name,
                quantity = foodItem.quantity,
                timestamp = foodItem.timestamp.toInstant(),
                notes = null
            )
        }
    }
}