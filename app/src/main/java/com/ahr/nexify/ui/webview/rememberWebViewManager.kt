package com.ahr.nexify.ui.webview

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.ahr.nexify.core.adblock.AdBlocker
import com.ahr.nexify.core.webview.WebViewManager

@Composable
internal fun rememberWebViewManager(
    context: Context,
    adBlocker: AdBlocker
): WebViewManager {
    return remember(context, adBlocker) {
        WebViewManager(context, adBlocker)
    }
}
