package com.tiarkaerell.ibstracker

import android.content.Context
import androidx.room.Room
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository

/**
 * Dependency injection container for the IBS Tracker app.
 *
 * Provides:
 * - AppDatabase with migrations (v1→v2, v2→v9)
 * - DataRepository with all DAOs
 * - SettingsRepository for app preferences
 */
class AppContainer(private val context: Context) {
    val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "ibs-tracker-database"
        )
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_9
        )
        .build()
    }

    val dataRepository: DataRepository by lazy {
        DataRepository(
            foodItemDao = appDatabase.foodItemDao(),
            commonFoodDao = appDatabase.commonFoodDao(),
            foodUsageStatsDao = appDatabase.foodUsageStatsDao(),
            symptomDao = appDatabase.symptomDao()
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    val appContext: Context = context.applicationContext
}