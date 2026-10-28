package com.ahr.nexify.core.startup

import com.ahr.nexify.core.activation.ActivationManager
import com.ahr.nexify.core.adblock.AdBlocker
import com.ahr.nexify.core.announcement.AnnouncementManager
import com.ahr.nexify.core.logging.AppLogger
import com.ahr.nexify.core.shell.ShellModeManager
import com.ahr.nexify.core.shell.ShellRuntimeServices

class ShellRuntimeStartup(
    private val shellModeManager: ShellModeManager,
    private val activationManager: ActivationManager,
    private val announcementManager: AnnouncementManager,
    private val adBlocker: AdBlocker,
) {

    fun initialize() {
        ShellRuntimeServices.initialize(
            shellModeManager = shellModeManager,
            activationManager = activationManager,
            announcementManager = announcementManager,
            adBlocker = adBlocker,
        )

        try {
            val isShell = shellModeManager.isShellMode()
            AppLogger.i("NexifyApplication", "Shell mode pre-check: $isShell")
        } catch (e: Exception) {
            AppLogger.e("NexifyApplication", "Shell mode pre-check failed", e)
        } catch (e: Error) {
            AppLogger.e("NexifyApplication", "Shell mode pre-check critical error", Error(e))
        }
    }

    fun shutdown() {
        ShellRuntimeServices.reset()
    }
}
