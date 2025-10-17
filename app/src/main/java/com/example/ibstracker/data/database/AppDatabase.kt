package com.example.ibstracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ibstracker.data.model.FoodItem
import com.example.ibstracker.data.model.Symptom

@Database(entities = [FoodItem::class, Symptom::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun symptomDao(): SymptomDao
}