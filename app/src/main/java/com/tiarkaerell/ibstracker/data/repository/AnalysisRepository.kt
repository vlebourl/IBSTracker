package com.tiarkaerell.ibstracker.data.repository

import com.tiarkaerell.ibstracker.data.model.AnalysisFilters
import com.tiarkaerell.ibstracker.data.model.AnalysisResult
import com.tiarkaerell.ibstracker.data.model.AnalysisTimeWindow
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface AnalysisRepository {
    suspend fun generateAnalysis(
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): AnalysisResult
    
    suspend fun getCachedAnalysis(
        timeWindow: AnalysisTimeWindow,
        filters: AnalysisFilters
    ): AnalysisResult?
    
    suspend fun invalidateCache(since: Instant)
    
    fun observeAnalysisResults(): Flow<AnalysisResult?>
}