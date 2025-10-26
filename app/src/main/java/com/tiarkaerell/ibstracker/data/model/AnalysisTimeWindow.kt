package com.tiarkaerell.ibstracker.data.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class AnalysisTimeWindow(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val windowSizeHours: Int = 8,
    val minimumOccurrences: Int = 3,
    val minimumObservationDays: Int = 14
) {
    fun isValid(): Boolean {
        return endDate.isAfter(startDate) && 
               windowSizeHours > 0 && 
               minimumOccurrences > 0 && 
               minimumObservationDays > 0 &&
               totalDays() >= minimumObservationDays
    }
    
    fun totalDays(): Int = ChronoUnit.DAYS.between(startDate, endDate).toInt()
    
    init {
        require(isValid()) { "AnalysisTimeWindow validation failed" }
    }
}