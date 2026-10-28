package com.ahr.nexify.core.startup

import com.ahr.nexify.core.logging.AppLogger
import com.ahr.nexify.core.stats.AppHealthMonitor
import com.ahr.nexify.data.repository.WebAppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class BackgroundServicesStartup(
    private val webAppRepository: WebAppRepository,
    private val healthMonitor: AppHealthMonitor,
    @Suppress("unused") private val screenshotService: com.ahr.nexify.core.stats.WebsiteScreenshotService,
) {

    fun initialize(appScope: CoroutineScope) {
        appScope.launch {
            try {
                healthMonitor.startMonitoring(webAppRepository.httpWebApps)
                AppLogger.i("NexifyApplication", "Health monitor started")
            } catch (e: Exception) {
                AppLogger.e("NexifyApplication", "Background services start failed", e)
            }
        }
    }

    fun shutdown() {
        try {
            healthMonitor.destroy()
        } catch (_: Exception) {
        }
    }
}
