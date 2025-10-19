package com.tiarkaerell.ibstracker.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tiarkaerell.ibstracker.data.database.dao.FoodItemDao
import com.tiarkaerell.ibstracker.data.database.dao.SymptomDao
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom

@Database(entities = [FoodItem::class, Symptom::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun symptomDao(): SymptomDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add category column to food_items table with default value 'OTHER'
                database.execSQL("ALTER TABLE food_items ADD COLUMN category TEXT NOT NULL DEFAULT 'OTHER'")
            }
        }
    }
}