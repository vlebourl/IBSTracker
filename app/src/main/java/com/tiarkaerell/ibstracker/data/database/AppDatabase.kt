package com.tiarkaerell.ibstracker.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tiarkaerell.ibstracker.data.database.dao.FoodItemDao
import com.tiarkaerell.ibstracker.data.database.dao.SymptomDao
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom

@Database(entities = [FoodItem::class, Symptom::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun symptomDao(): SymptomDao
}