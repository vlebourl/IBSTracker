package com.example.ibstracker

import android.content.Context
import androidx.room.Room
import com.example.ibstracker.data.database.AppDatabase
import com.example.ibstracker.data.repository.DataRepository

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
}