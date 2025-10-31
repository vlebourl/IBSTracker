package com.tiarkaerell.ibstracker

import android.app.Application
import com.tiarkaerell.ibstracker.data.backup.GoogleDriveBackupWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class IBSTrackerApplication : Application() {
    lateinit var container: AppContainer
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Schedule daily cloud backup sync (2:00 AM) if Cloud Sync is enabled
        // This ensures the job is scheduled on app startup based on user preferences
        applicationScope.launch {
            val settings = container.backupPreferences.settingsFlow.first()
            if (settings.cloudSyncEnabled) {
                android.util.Log.d("IBSTrackerApplication", "Cloud sync enabled - scheduling WorkManager job on app startup")
                GoogleDriveBackupWorker.schedule(this@IBSTrackerApplication)
            } else {
                android.util.Log.d("IBSTrackerApplication", "Cloud sync disabled - not scheduling WorkManager job on app startup")
            }
        }
    }
}