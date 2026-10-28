package com.ahr.nexify.ui.webview

import com.ahr.nexify.core.activation.ActivationManager
import com.ahr.nexify.core.adblock.AdBlocker
import com.ahr.nexify.core.announcement.AnnouncementManager
import com.ahr.nexify.core.webview.LocalHttpServer
import com.ahr.nexify.data.repository.WebAppRepository

data class WebViewScreenDependencies(
    val repository: WebAppRepository,
    val activation: ActivationManager,
    val announcement: AnnouncementManager,
    val adBlocker: AdBlocker,
    val localHttpServer: LocalHttpServer,
)
