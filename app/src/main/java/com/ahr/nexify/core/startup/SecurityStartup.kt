package com.ahr.nexify.core.startup

import android.content.Context
import com.ahr.nexify.core.crypto.SecurityInitializer
import com.ahr.nexify.core.logging.AppLogger

class SecurityStartup(private val context: Context) {

    fun initialize() {
        try {
            val initialized = SecurityInitializer.initialize(context) { result ->
                AppLogger.w(
                    "NexifyApplication",
                    "Security threat detected: level=${result.threatLevel}, block=${result.shouldBlock}, threats=${result.threats}"
                )
            }
            AppLogger.i("NexifyApplication", "Security initializer status: $initialized")
        } catch (e: Exception) {
            AppLogger.e("NexifyApplication", "Security initialization failed", e)
        } catch (e: Error) {
            AppLogger.e("NexifyApplication", "Security initialization critical error", Error(e))
        }
    }

    fun shutdown() {
        SecurityInitializer.shutdown()
    }
}
