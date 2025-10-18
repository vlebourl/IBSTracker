package com.tiarkaerell.ibstracker.data.repository

import com.tiarkaerell.ibstracker.data.database.dao.FoodItemDao
import com.tiarkaerell.ibstracker.data.database.dao.SymptomDao
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom
import kotlinx.coroutines.flow.Flow

class DataRepository(
    private val foodItemDao: FoodItemDao,
    private val symptomDao: SymptomDao
) {
    fun getAllFoodItems(): Flow<List<FoodItem>> = foodItemDao.getAll()

    suspend fun insertFoodItem(foodItem: FoodItem) {
        foodItemDao.insert(foodItem)
    }

    suspend fun updateFoodItem(foodItem: FoodItem) {
        foodItemDao.update(foodItem)
    }

    suspend fun deleteFoodItem(foodItem: FoodItem) {
        foodItemDao.delete(foodItem)
    }

    fun getAllSymptoms(): Flow<List<Symptom>> = symptomDao.getAll()

    suspend fun insertSymptom(symptom: Symptom) {
        symptomDao.insert(symptom)
    }

    suspend fun updateSymptom(symptom: Symptom) {
        symptomDao.update(symptom)
    }

    suspend fun deleteSymptom(symptom: Symptom) {
        symptomDao.delete(symptom)
    }
}