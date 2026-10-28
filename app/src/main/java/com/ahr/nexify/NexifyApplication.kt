package com.ahr.nexify

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.ahr.nexify.core.activation.ActivationManager
import com.ahr.nexify.core.agent.mcp.HostMcpController
import com.ahr.nexify.core.adblock.AdBlocker
import com.ahr.nexify.core.announcement.AnnouncementManager
import com.ahr.nexify.core.i18n.Strings
import com.ahr.nexify.core.image.OptimizedImageLoader
import com.ahr.nexify.core.logging.AppLogger
import com.ahr.nexify.core.shell.ShellModeManager
import com.ahr.nexify.core.startup.AppStartupManager
import com.ahr.nexify.core.startup.BackgroundServicesStartup
import com.ahr.nexify.core.startup.LegacyHttpUrlMigrationStartup
import com.ahr.nexify.core.startup.LoggingStartup
import com.ahr.nexify.core.startup.RuntimeWarmupStartup
import com.ahr.nexify.core.startup.SecurityStartup
import com.ahr.nexify.core.startup.ShellRuntimeStartup
import com.ahr.nexify.data.database.AppDatabase
import com.ahr.nexify.data.repository.WebAppRepository
import com.ahr.nexify.data.repository.AppCategoryRepository
import com.ahr.nexify.di.appModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class NexifyApplication : Application(), ImageLoaderFactory {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appStartupManager: AppStartupManager? = null
    private var shellActivationManager: ActivationManager? = null
    private var shellAnnouncementManager: AnnouncementManager? = null
    private var shellAdBlocker: AdBlocker? = null
    private var shellModeManagerLocal: ShellModeManager? = null

    val database: AppDatabase by inject()
    val webAppRepository: WebAppRepository by inject()
    val appCategoryRepository: AppCategoryRepository by inject()
    val activationManager: ActivationManager by inject()
    val announcementManager: AnnouncementManager by inject()
    val adBlocker: AdBlocker by inject()
    val shellModeManager: ShellModeManager by inject()
    val healthMonitor: com.ahr.nexify.core.stats.AppHealthMonitor by inject()
    val screenshotService: com.ahr.nexify.core.stats.WebsiteScreenshotService by inject()

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Gecko child processes (:tab*/:gpu/...) get a fresh PathClassLoader and
        // their service onCreate loads mozglue before any of our code could run
        // later — the native lib graft has to happen here, in every process.
        // Gecko children additionally need the dep .so files resident up front.
        val provisioner = com.ahr.nexify.core.engine.GeckoRuntimeProvisioner
        provisioner.ensureNativeLibsVisible(base)
        if (provisioner.isGeckoChildProcess()) {
            provisioner.preloadDownloadedLibs(base)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Strings.initialize(this)

        if (isNodeJsProcess()) {
            try {
                AppLogger.init(this)
                AppLogger.system("Application", ":nodejs 子进程 onCreate (跳过宿主重型 init)")
            } catch (e: Exception) {
                android.util.Log.e("NexifyApplication", ":nodejs 子进程 logger init 失败", e)
            }
            return
        }

        if (BuildConfig.SHELL_RUNTIME_ONLY) {
            initShellRuntime()
            AppLogger.system("Application", "onCreate completed (shell)")
            return
        }

        // Before any activity. Shortcut cold starts never compose the main
        // screen, which is the only other place that publishes Strings.lang.
        com.ahr.nexify.core.i18n.HostLanguage.applyBlocking(this)

        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidLogger(Level.ERROR)
                androidContext(this@NexifyApplication)
                modules(appModules)
            }
        }

        appStartupManager = AppStartupManager(
            loggingStartup = LoggingStartup(this),
            shellRuntimeStartup = ShellRuntimeStartup(
                shellModeManager = shellModeManager,
                activationManager = activationManager,
                announcementManager = announcementManager,
                adBlocker = adBlocker,
            ),
            securityStartup = SecurityStartup(this),
            runtimeWarmupStartup = RuntimeWarmupStartup(
                appContext = this,
            ),
            legacyHttpUrlMigrationStartup = LegacyHttpUrlMigrationStartup(
                context = this,
                webAppRepository = webAppRepository,
            ),
            backgroundServicesStartup = BackgroundServicesStartup(
                webAppRepository = webAppRepository,
                healthMonitor = healthMonitor,
                screenshotService = screenshotService,
            ),
            database = database,
        ).also { it.initialize(appScope) }

        appScope.launch {
            runCatching { HostMcpController.restore(this@NexifyApplication) }
                .onFailure { AppLogger.w("Application", "local MCP restore failed: ${it.message}") }
        }
    }

    override fun onTerminate() {
        if (BuildConfig.SHELL_RUNTIME_ONLY) {
            cleanupSingletons()
            AppLogger.shutdown()
        } else {
            appStartupManager?.shutdown(appScope)
        }
        super.onTerminate()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        AppLogger.w("NexifyApplication", "onLowMemory triggered")
        clearAppCaches()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        val levelName = when (level) {
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "RUNNING_MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "RUNNING_LOW"
            ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "RUNNING_CRITICAL"
            ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> "UI_HIDDEN"
            ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "BACKGROUND"
            ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "MODERATE"
            ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "COMPLETE"
            else -> "UNKNOWN($level)"
        }
        AppLogger.d("NexifyApplication", "onTrimMemory: $levelName")
        if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            clearAppCaches()
        }
    }

    private fun clearAppCaches() {
        if (BuildConfig.SHELL_RUNTIME_ONLY) {
            try {
                com.ahr.nexify.core.crypto.AesCryptoEngine.clearKeyCache()
                com.ahr.nexify.util.HtmlProjectProcessor.clearEncodingCache()
                AppLogger.i("NexifyApplication", "App caches cleared")
            } catch (e: Exception) {
                AppLogger.e("NexifyApplication", "Failed to clear app caches", e)
            }
            return
        }

        appStartupManager?.clearCaches()
    }

    private fun cleanupSingletons() {
        try {
            AppLogger.d("NexifyApplication", "Cleaning up singleton resources...")

            if (BuildConfig.SHELL_RUNTIME_ONLY) {
                shellActivationManager = null
                shellAnnouncementManager = null
                shellAdBlocker = null
                shellModeManagerLocal = null
                AppLogger.i("NexifyApplication", "Shell runtime resources cleaned up")
                return
            }
        } catch (e: Exception) {
            AppLogger.e("NexifyApplication", "Failed to cleanup singleton resources", e)
        }
    }

    private fun initShellRuntime() {
        shellModeManagerLocal = ShellModeManager(this)
        shellActivationManager = ActivationManager(this)
        shellAnnouncementManager = AnnouncementManager(this)
        shellAdBlocker = AdBlocker()

        try {
            val isShell = shellModeManagerLocal?.isShellMode() == true
            AppLogger.i("NexifyApplication", "Dedicated shell runtime initialized: shellMode=$isShell")
        } catch (e: Exception) {
            AppLogger.e("NexifyApplication", "Shell runtime initialization failed", e)
        }
    }

    private fun isNodeJsProcess(): Boolean {
        return try {
            val processName = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                Application.getProcessName()
            } else {
                runCatching {
                    val am = getSystemService(android.content.Context.ACTIVITY_SERVICE)
                        as android.app.ActivityManager
                    val pid = android.os.Process.myPid()
                    am.runningAppProcesses?.firstOrNull { it.pid == pid }?.processName
                }.getOrNull()
            }
            processName?.endsWith(":nodejs") == true
        } catch (_: Exception) {
            false
        }
    }

    override fun newImageLoader(): ImageLoader = OptimizedImageLoader.get(this)

    companion object {
        private lateinit var instance: NexifyApplication

        fun getInstance(): NexifyApplication = instance

        @Deprecated("编辑器侧请使用 Koin 注入: val repo: WebAppRepository by inject()", ReplaceWith("org.koin.java.KoinJavaComponent.get(WebAppRepository::class.java)"))
        val repository: WebAppRepository
            get() = instance.webAppRepository

        @Deprecated("编辑器侧请使用 Koin 注入: val repo: AppCategoryRepository by inject()", ReplaceWith("org.koin.java.KoinJavaComponent.get(AppCategoryRepository::class.java)"))
        val categoryRepository: AppCategoryRepository
            get() = instance.appCategoryRepository

        @Deprecated("编辑器侧请使用 Koin 注入: val mgr: ActivationManager by inject()", ReplaceWith("org.koin.java.KoinJavaComponent.get(ActivationManager::class.java)"))
        val activation: ActivationManager
            get() = if (BuildConfig.SHELL_RUNTIME_ONLY) {
                instance.shellActivationManager ?: error("Shell ActivationManager unavailable")
            } else {
                instance.activationManager
            }

        @Deprecated("编辑器侧请使用 Koin 注入: val mgr: AnnouncementManager by inject()", ReplaceWith("org.koin.java.KoinJavaComponent.get(AnnouncementManager::class.java)"))
        val announcement: AnnouncementManager
            get() = if (BuildConfig.SHELL_RUNTIME_ONLY) {
                instance.shellAnnouncementManager ?: error("Shell AnnouncementManager unavailable")
            } else {
                instance.announcementManager
            }

        @Deprecated("编辑器侧请使用 Koin 注入: val blocker: AdBlocker by inject()", ReplaceWith("org.koin.java.KoinJavaComponent.get(AdBlocker::class.java)"))
        val adBlock: AdBlocker
            get() = if (BuildConfig.SHELL_RUNTIME_ONLY) {
                instance.shellAdBlocker ?: error("Shell AdBlocker unavailable")
            } else {
                instance.adBlocker
            }

        @Deprecated("编辑器侧请使用 Koin 注入: val mgr: ShellModeManager by inject()", ReplaceWith("org.koin.java.KoinJavaComponent.get(ShellModeManager::class.java)"))
        val shellMode: ShellModeManager
            get() = if (BuildConfig.SHELL_RUNTIME_ONLY) {
                instance.shellModeManagerLocal ?: error("ShellModeManager unavailable")
            } else {
                instance.shellModeManager
            }
    }
}
