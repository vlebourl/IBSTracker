package com.tiarkaerell.ibstracker.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tiarkaerell.ibstracker.data.model.Symptom
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Insert
    suspend fun insert(symptom: Symptom)

    @Update
    suspend fun update(symptom: Symptom)

    @Delete
    suspend fun delete(symptom: Symptom)

    @Query("SELECT * FROM symptoms ORDER BY date DESC")
    fun getAll(): Flow<List<Symptom>>
}