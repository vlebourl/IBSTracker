package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.*
import com.tiarkaerell.ibstracker.data.model.CorrelationCache
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface CorrelationCacheDao {
    
    @Query("SELECT * FROM correlation_cache WHERE is_valid = 1")
    fun getAllValidCache(): Flow<List<CorrelationCache>>
    
    @Query("""
        SELECT * FROM correlation_cache 
        WHERE food_name = :foodName 
        AND symptom_type = :symptomType 
        AND filters = :filters 
        AND time_window = :timeWindow 
        AND is_valid = 1
        LIMIT 1
    """)
    suspend fun getCachedCorrelation(
        foodName: String,
        symptomType: String,
        filters: String,
        timeWindow: String
    ): CorrelationCache?
    
    @Query("""
        SELECT * FROM correlation_cache 
        WHERE data_hash_code = :dataHashCode 
        AND is_valid = 1
    """)
    suspend fun getCacheByDataHash(dataHashCode: Int): List<CorrelationCache>
    
    @Query("SELECT * FROM correlation_cache WHERE calculated_at < :before")
    suspend fun getExpiredCache(before: Instant): List<CorrelationCache>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cache: CorrelationCache): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(caches: List<CorrelationCache>)
    
    @Update
    suspend fun update(cache: CorrelationCache): Int
    
    @Delete
    suspend fun delete(cache: CorrelationCache): Int
    
    @Query("UPDATE correlation_cache SET is_valid = 0 WHERE data_hash_code = :dataHashCode")
    suspend fun invalidateCacheByDataHash(dataHashCode: Int): Int
    
    @Query("UPDATE correlation_cache SET is_valid = 0 WHERE calculated_at < :before")
    suspend fun invalidateExpiredCache(before: Instant): Int
    
    @Query("DELETE FROM correlation_cache WHERE is_valid = 0")
    suspend fun deleteInvalidCache(): Int
    
    @Query("DELETE FROM correlation_cache")
    suspend fun deleteAll(): Int
    
    @Query("SELECT COUNT(*) FROM correlation_cache WHERE is_valid = 1")
    fun getValidCacheCount(): Flow<Int>
}