package com.tiarkaerell.ibstracker

import android.app.Application
import com.tiarkaerell.ibstracker.data.backup.GoogleDriveBackupWorker

class IBSTrackerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Schedule daily cloud backup sync (2:00 AM, WiFi + charging)
        GoogleDriveBackupWorker.schedule(this)
    }
}