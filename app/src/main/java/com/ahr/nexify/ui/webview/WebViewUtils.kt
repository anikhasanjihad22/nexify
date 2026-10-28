package com.ahr.nexify.ui.webview

import com.ahr.nexify.core.logging.AppLogger
import com.ahr.nexify.data.model.WebApp
import com.ahr.nexify.util.ensureWebUrlScheme
import com.ahr.nexify.util.normalizeExternalIntentUrl

internal fun normalizeWebUrlForSecurity(rawUrl: String?): String {
    val trimmed = rawUrl?.trim().orEmpty()

    return ensureWebUrlScheme(trimmed)
}

internal fun normalizeExternalUrlForIntent(rawUrl: String): String {
    val safeUrl = normalizeExternalIntentUrl(rawUrl)
    if (safeUrl.isEmpty()) {
        AppLogger.w("WebViewActivity", "Blocked invalid or dangerous external URL: $rawUrl")
        return ""
    }
    return normalizeWebUrlForSecurity(safeUrl)
}

internal fun hasConfiguredAds(app: WebApp): Boolean {
    val config = app.adConfig
    return app.adsEnabled ||
        config?.bannerId?.isNotBlank() == true ||
        config?.interstitialId?.isNotBlank() == true ||
        config?.splashId?.isNotBlank() == true
}
