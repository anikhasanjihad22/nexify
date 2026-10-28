package com.ahr.nexify.core.startup

import com.ahr.nexify.core.logging.AppLogger
import com.ahr.nexify.core.port.PortManager
import com.ahr.nexify.core.shell.ShellRuntimeServices
import com.ahr.nexify.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AppStartupManager(
    private val loggingStartup: LoggingStartup,
    private val shellRuntimeStartup: ShellRuntimeStartup,
    private val securityStartup: SecurityStartup,
    private val runtimeWarmupStartup: RuntimeWarmupStartup,
    private val legacyHttpUrlMigrationStartup: LegacyHttpUrlMigrationStartup,
    private val backgroundServicesStartup: BackgroundServicesStartup,
    private val database: AppDatabase,
) {

    fun initialize(appScope: CoroutineScope) {
        loggingStartup.initialize()
        shellRuntimeStartup.initialize()

        appScope.launch(Dispatchers.Default) {
            securityStartup.initialize()
        }
        legacyHttpUrlMigrationStartup.initialize(appScope)
        runtimeWarmupStartup.initialize(appScope)
        backgroundServicesStartup.initialize(appScope)
        AppLogger.system("Application", "onCreate completed")
    }

    fun clearCaches() {
        try {
            com.ahr.nexify.core.crypto.AesCryptoEngine.clearKeyCache()
            com.ahr.nexify.util.HtmlProjectProcessor.clearEncodingCache()
            AppLogger.i("NexifyApplication", "App caches cleared")
        } catch (e: Exception) {
            AppLogger.e("NexifyApplication", "Failed to clear app caches", e)
        }
    }

    fun shutdown(appScope: CoroutineScope) {
        AppLogger.system("Application", "onTerminate started")

        appScope.coroutineContext[Job]?.cancel()
        backgroundServicesStartup.shutdown()
        PortManager.releaseAll()
        com.ahr.nexify.util.OfflineManager.release()
        com.ahr.nexify.core.plugin.PluginStore.release()
        com.ahr.nexify.util.DownloadNotificationManager.release()
        com.ahr.nexify.core.crypto.AesCryptoEngine.clearKeyCache()
        runtimeWarmupStartup.shutdown()
        securityStartup.shutdown()
        database.close()
        ShellRuntimeServices.reset()
        loggingStartup.shutdown()
    }
}
