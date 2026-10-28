package com.ahr.nexify.core.startup

import android.content.Context
import com.ahr.nexify.core.logging.AppLogger

class LoggingStartup(private val context: Context) {

    fun initialize() {
        try {
            AppLogger.init(context)
            AppLogger.system("Application", "onCreate started")
        } catch (e: Exception) {
            android.util.Log.e("NexifyApplication", "AppLogger initialization failed", e)
        }
    }

    fun shutdown() {
        AppLogger.shutdown()
    }
}
