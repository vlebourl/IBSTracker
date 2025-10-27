package com.tiarkaerell.ibstracker.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    fun applyLocale(activity: Activity, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(activity.resources.configuration)
        config.setLocale(locale)

        // Use createConfigurationContext instead of deprecated updateConfiguration
        // The context should be attached in Activity.attachBaseContext() for proper locale application
        activity.createConfigurationContext(config)

        // Note: For runtime locale changes, activity must be recreated to properly apply the new locale
        // Use activity.recreate() if changing locale while app is running
    }
}
