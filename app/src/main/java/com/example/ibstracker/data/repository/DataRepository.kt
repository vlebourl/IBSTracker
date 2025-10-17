package com.example.ibstracker.data.repository

import com.example.ibstracker.data.database.dao.FoodItemDao
import com.example.ibstracker.data.database.dao.SymptomDao
import com.example.ibstracker.data.model.FoodItem
import com.example.ibstracker.data.model.Symptom
import kotlinx.coroutines.flow.Flow

class DataRepository(
    private val foodItemDao: FoodItemDao,
    private val symptomDao: SymptomDao
) {
    fun getAllFoodItems(): Flow<List<FoodItem>> = foodItemDao.getAll()

    suspend fun insertFoodItem(foodItem: FoodItem) {
        foodItemDao.insert(foodItem)
    }

    fun getAllSymptoms(): Flow<List<Symptom>> = symptomDao.getAll()

    suspend fun insertSymptom(symptom: Symptom) {
        symptomDao.insert(symptom)
    }
}