package com.tiarkaerell.ibstracker

import android.app.Application

class IBSTrackerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}