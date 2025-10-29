package com.tiarkaerell.ibstracker

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tiarkaerell.ibstracker.data.analysis.*
import com.tiarkaerell.ibstracker.data.backup.BackupManager
import com.tiarkaerell.ibstracker.data.backup.GoogleDriveService
import com.tiarkaerell.ibstracker.data.backup.RestoreManager
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.preferences.BackupPreferences
import com.tiarkaerell.ibstracker.data.repository.AnalysisRepository
import com.tiarkaerell.ibstracker.data.repository.AnalysisRepositoryImpl
import com.tiarkaerell.ibstracker.data.repository.BackupRepository
import com.tiarkaerell.ibstracker.data.repository.BackupRepositoryImpl
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
 * - AnalysisRepository with analysis engine
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

    val backupManager: BackupManager by lazy {
        BackupManager(
            context = context,
            database = appDatabase,
            databaseVersion = 10 // AppDatabase current version
        )
    }

    private val restoreManager: RestoreManager by lazy {
        RestoreManager(
            context = context,
            database = appDatabase,
            backupManager = backupManager,
            currentDatabaseVersion = 10
        )
    }

    val backupPreferences: BackupPreferences by lazy {
        BackupPreferences(context)
    }

    val googleDriveService: GoogleDriveService by lazy {
        GoogleDriveService(
            context = context,
            database = appDatabase,
            settingsRepository = settingsRepository
        )
    }

    val backupRepository: BackupRepository by lazy {
        BackupRepositoryImpl(
            backupManager = backupManager,
            restoreManager = restoreManager,
            googleDriveService = googleDriveService,
            backupPreferences = backupPreferences
        )
    }

    val dataRepository: DataRepository by lazy {
        DataRepository(
            foodItemDao = appDatabase.foodItemDao(),
            commonFoodDao = appDatabase.commonFoodDao(),
            foodUsageStatsDao = appDatabase.foodUsageStatsDao(),
            symptomDao = appDatabase.symptomDao(),
            backupManager = backupManager
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }

    // Analysis dependencies
    private val dataAdapter: DataAdapter by lazy {
        DataAdapter(dataRepository)
    }

    private val correlationCalculator: CorrelationCalculator by lazy {
        CorrelationCalculator()
    }

    private val probabilityEngine: ProbabilityEngine by lazy {
        ProbabilityEngine(correlationCalculator)
    }

    private val triggerAnalyzer: TriggerAnalyzer by lazy {
        TriggerAnalyzer(probabilityEngine, dataAdapter)
    }

    val analysisRepository: AnalysisRepository by lazy {
        AnalysisRepositoryImpl(triggerAnalyzer)
    }

    val appContext: Context = context.applicationContext
}