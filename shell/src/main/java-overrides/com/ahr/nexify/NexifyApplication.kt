package com.ahr.nexify

import android.app.Application
import android.content.ComponentCallbacks2
import android.webkit.WebView
import com.ahr.nexify.core.activation.ActivationManager
import com.ahr.nexify.core.adblock.AdBlocker
import com.ahr.nexify.core.announcement.AnnouncementManager
import com.ahr.nexify.core.logging.AppLogger
import com.ahr.nexify.core.shell.ShellModeManager

class NexifyApplication : Application() {

    private var shellActivationManager: ActivationManager? = null
    private var shellAnnouncementManager: AnnouncementManager? = null
    private var shellAdBlocker: AdBlocker? = null
    private var shellModeManagerLocal: ShellModeManager? = null

    override fun onCreate() {
        super.onCreate()
        instance = this

        com.ahr.nexify.core.i18n.Strings.initialize(this)

        // NodeService runs in a dedicated :nodejs OS process (V8 lifecycle isolation).
        // The host deliberately skips its heavy init there; the shell override must do
        // the same or every Node app spawns a full runtime init (config parse,
        // ActivationManager, AdBlocker, perf optimizer) in the Node subprocess.
        if (isNodeJsProcess()) {
            try {
                AppLogger.init(this)
                AppLogger.system("Application", ":nodejs 子进程 onCreate (跳过 shell 重型 init)")
            } catch (e: Exception) {
                android.util.Log.e("NexifyApplication", ":nodejs 子进程 logger init 失败", e)
            }
            return
        }

        shellModeManagerLocal = ShellModeManager(this)
        val fileLoggingEnabled = try {
            shellModeManagerLocal?.getConfig()?.loggingEnabled ?: false
        } catch (e: Exception) {
            false
        }

        try {
            AppLogger.init(this, fileLoggingEnabled = fileLoggingEnabled)
            AppLogger.system("Application", "onCreate started (shell)")
        } catch (e: Exception) {
            android.util.Log.e("NexifyApplication", "AppLogger initialization failed", e)
        }

        initShellRuntime()

        com.ahr.nexify.core.perf.SystemPerfOptimizer.initSystem(this)
        com.ahr.nexify.core.perf.SystemPerfOptimizer.readaheadCriticalFiles(this)

        AppLogger.system("Application", "onCreate completed (shell)")
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

    override fun onTerminate() {
        AppLogger.system("Application", "onTerminate started (shell)")
        cleanupSingletons()
        AppLogger.shutdown()
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
        try {
            com.ahr.nexify.core.crypto.AesCryptoEngine.clearKeyCache()
            com.ahr.nexify.util.HtmlProjectProcessor.clearEncodingCache()
            AppLogger.i("NexifyApplication", "App caches cleared")
        } catch (e: Exception) {
            AppLogger.e("NexifyApplication", "Failed to clear app caches", e)
        }
    }

    private fun cleanupSingletons() {
        try {
            AppLogger.d("NexifyApplication", "Cleaning up singleton resources...")
            shellActivationManager = null
            shellAnnouncementManager = null
            shellAdBlocker = null
            shellModeManagerLocal = null

            com.ahr.nexify.core.crypto.AesCryptoEngine.clearKeyCache()
            com.ahr.nexify.core.webview.WebViewPool.release()
            com.ahr.nexify.core.perf.SystemPerfOptimizer.release()
            com.ahr.nexify.core.port.PortManager.releaseAll()
            com.ahr.nexify.util.DownloadNotificationManager.release()

            AppLogger.i("NexifyApplication", "Shell runtime resources cleaned up")
        } catch (e: Exception) {
            AppLogger.e("NexifyApplication", "Failed to cleanup singleton resources", e)
        }
    }

    private fun initShellRuntime() {
        if (shellModeManagerLocal == null) {
            shellModeManagerLocal = ShellModeManager(this)
        }
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

    companion object {
        private lateinit var instance: NexifyApplication

        fun getInstance(): NexifyApplication = instance

        val activation: ActivationManager
            get() = instance.shellActivationManager ?: error("Shell ActivationManager unavailable")

        val announcement: AnnouncementManager
            get() = instance.shellAnnouncementManager ?: error("Shell AnnouncementManager unavailable")

        val adBlock: AdBlocker
            get() = instance.shellAdBlocker ?: error("Shell AdBlocker unavailable")

        val shellMode: ShellModeManager
            get() = instance.shellModeManagerLocal ?: error("ShellModeManager unavailable")
    }
}
