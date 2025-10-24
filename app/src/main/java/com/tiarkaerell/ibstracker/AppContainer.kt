package com.tiarkaerell.ibstracker

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository
import com.tiarkaerell.ibstracker.util.PrePopulatedFoods
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Dependency injection container for the IBS Tracker app.
 *
 * Provides:
 * - AppDatabase with migrations (v1→v2, v2→v9, v9→v10) and onCreate callback
 * - DataRepository with all DAOs
 * - SettingsRepository for app preferences
 */
class AppContainer(private val context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val appDatabase: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "ibs-tracker-database"
    )
    .addMigrations(
        AppDatabase.MIGRATION_1_2,
        AppDatabase.MIGRATION_2_9,
        AppDatabase.MIGRATION_9_10
    )
    .addCallback(DatabaseCallback(applicationScope))
    .build()
    .also { database ->
        // Populate common_foods if empty (works for fresh install and post-migration)
        applicationScope.launch {
            val count = database.commonFoodDao().getCommonFoodCount().first()
            if (count == 0) {
                database.commonFoodDao().insertAll(PrePopulatedFoods.foods)
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        // Callback kept for potential future use
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