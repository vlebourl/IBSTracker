package com.tiarkaerell.ibstracker

import android.content.Context
import androidx.room.Room
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.repository.DataRepository
import com.tiarkaerell.ibstracker.data.repository.SettingsRepository

class AppContainer(private val context: Context) {
    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "ibs-tracker-database"
        ).build()
    }

    val dataRepository: DataRepository by lazy {
        DataRepository(appDatabase.foodItemDao(), appDatabase.symptomDao())
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context)
    }
}