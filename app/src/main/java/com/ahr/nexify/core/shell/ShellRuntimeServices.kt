package com.ahr.nexify.core.shell

import com.ahr.nexify.core.activation.ActivationManager
import com.ahr.nexify.core.adblock.AdBlocker
import com.ahr.nexify.core.announcement.AnnouncementManager

object ShellRuntimeServices {

    private var shellModeManager: ShellModeManager? = null
    private var activationManager: ActivationManager? = null
    private var announcementManager: AnnouncementManager? = null
    private var adBlocker: AdBlocker? = null

    fun initialize(
        shellModeManager: ShellModeManager,
        activationManager: ActivationManager,
        announcementManager: AnnouncementManager,
        adBlocker: AdBlocker,
    ) {
        this.shellModeManager = shellModeManager
        this.activationManager = activationManager
        this.announcementManager = announcementManager
        this.adBlocker = adBlocker
    }

    fun reset() {
        shellModeManager = null
        activationManager = null
        announcementManager = null
        adBlocker = null
    }

    val shellMode: ShellModeManager
        get() = requireNotNull(shellModeManager) { "ShellRuntimeServices 尚未初始化 shellMode" }

    val activation: ActivationManager
        get() = requireNotNull(activationManager) { "ShellRuntimeServices 尚未初始化 activation" }

    val announcement: AnnouncementManager
        get() = requireNotNull(announcementManager) { "ShellRuntimeServices 尚未初始化 announcement" }

    val adBlock: AdBlocker
        get() = requireNotNull(adBlocker) { "ShellRuntimeServices 尚未初始化 adBlock" }
}
