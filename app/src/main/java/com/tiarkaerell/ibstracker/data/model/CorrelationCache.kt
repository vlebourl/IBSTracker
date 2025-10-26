package com.tiarkaerell.ibstracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.util.UUID

@Entity(tableName = "correlation_cache")
data class CorrelationCache(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "food_name") val foodName: String,
    @ColumnInfo(name = "symptom_type") val symptomType: String,
    @ColumnInfo(name = "correlation_score") val correlationScore: Double,
    @ColumnInfo(name = "confidence") val confidence: Double,
    @ColumnInfo(name = "calculated_at") val calculatedAt: Instant,
    @ColumnInfo(name = "data_hash_code") val dataHashCode: Int,
    @ColumnInfo(name = "filters") val filters: String,
    @ColumnInfo(name = "time_window") val timeWindow: String,
    @ColumnInfo(name = "is_valid") val isValid: Boolean = true
) {
    init {
        require(correlationScore in 0.0..1.0) { 
            "correlationScore must be between 0.0 and 1.0" 
        }
        require(confidence in 0.0..1.0) { 
            "confidence must be between 0.0 and 1.0" 
        }
        require(!calculatedAt.isAfter(Instant.now())) { 
            "calculatedAt must not be in the future" 
        }
    }
}