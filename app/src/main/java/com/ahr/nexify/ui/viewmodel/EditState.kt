package com.ahr.nexify.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.Stable
import com.ahr.nexify.core.activation.ActivationCode
import com.ahr.nexify.core.appearance.DeviceDisguiseConfig
import com.ahr.nexify.data.model.ActivationDialogConfig
import com.ahr.nexify.data.model.AdConfig
import com.ahr.nexify.data.model.Announcement
import com.ahr.nexify.data.model.ApkExportConfig
import com.ahr.nexify.data.model.withRuntimePermissionsSyncedFromFeatures
import com.ahr.nexify.data.model.AppType
import com.ahr.nexify.data.model.AutoStartConfig
import com.ahr.nexify.data.model.BgmConfig
import com.ahr.nexify.data.model.HtmlConfig
import com.ahr.nexify.data.model.MediaConfig
import com.ahr.nexify.data.model.SplashConfig
import com.ahr.nexify.data.model.TranslateConfig
import com.ahr.nexify.data.model.WebApp
import com.ahr.nexify.data.model.WebViewConfig

@Stable
data class EditState(
    val name: String = "",
    val url: String = "",
    val iconUri: Uri? = null,
    val savedIconPath: String? = null,
    val iconBitmap: Bitmap? = null,
    val appType: AppType = AppType.WEB,
    val mediaConfig: MediaConfig? = null,
    val htmlConfig: HtmlConfig? = null,
    val activationEnabled: Boolean = false,
    val activationCodeList: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.ahr.nexify.data.model.RemoteActivationConfig? = null,
    val adsEnabled: Boolean = false,
    val adConfig: AdConfig = AdConfig(),
    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement(),
    val adBlockEnabled: Boolean = false,
    val adBlockRules: List<String> = emptyList(),
    val adBlockSubscriptions: List<String> = emptyList(),
    val webViewConfig: WebViewConfig = WebViewConfig(),
    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),
    val splashMediaUri: Uri? = null,
    val savedSplashPath: String? = null,
    val bgmEnabled: Boolean = false,
    val bgmConfig: BgmConfig = BgmConfig(),
    val apkExportConfig: ApkExportConfig = ApkExportConfig(),
    val themeType: String = "AURORA",
    val translateEnabled: Boolean = false,
    val translateConfig: TranslateConfig = TranslateConfig(),
    val pluginsEnabled: Boolean = false,
    val pluginIds: Set<String> = emptySet(),
    val autoStartConfig: AutoStartConfig? = null,
    val deviceDisguiseConfig: DeviceDisguiseConfig = DeviceDisguiseConfig(),
)

fun WebApp.toEditState(): EditState {
    val synced = withRuntimePermissionsSyncedFromFeatures()
    return EditState(
        name = synced.name,
        url = synced.url,
        iconUri = synced.iconPath?.let(Uri::parse),
        savedIconPath = synced.iconPath,
        appType = synced.appType,
        mediaConfig = synced.mediaConfig,
        htmlConfig = synced.htmlConfig,
        activationEnabled = synced.activationEnabled,
        activationCodeList = synced.activationCodeList,
        activationRequireEveryTime = synced.activationRequireEveryTime,
        activationDialogConfig = synced.activationDialogConfig ?: ActivationDialogConfig(),
        activationRemoteConfig = synced.activationRemoteConfig,
        adsEnabled = synced.adsEnabled,
        adConfig = synced.adConfig ?: AdConfig(),
        announcementEnabled = synced.announcementEnabled,
        announcement = synced.announcement ?: Announcement(),
        adBlockEnabled = synced.adBlockEnabled,
        adBlockRules = synced.adBlockRules,
        adBlockSubscriptions = synced.adBlockSubscriptions,
        webViewConfig = synced.webViewConfig,
        splashEnabled = synced.splashEnabled,
        splashConfig = synced.splashConfig ?: SplashConfig(),
        splashMediaUri = synced.splashConfig?.mediaPath?.let(Uri::parse),
        savedSplashPath = synced.splashConfig?.mediaPath,
        bgmEnabled = synced.bgmEnabled,
        bgmConfig = synced.bgmConfig ?: BgmConfig(),
        apkExportConfig = synced.apkExportConfig ?: ApkExportConfig(),
        themeType = synced.themeType,
        translateEnabled = synced.translateEnabled,
        translateConfig = synced.translateConfig ?: TranslateConfig(),
        pluginsEnabled = synced.pluginsEnabled,
        pluginIds = synced.pluginIds.toSet(),
        autoStartConfig = synced.autoStartConfig,
        deviceDisguiseConfig = synced.deviceDisguiseConfig ?: DeviceDisguiseConfig(),
    )
}

fun EditState.withRuntimePermissionsSyncedFromFeatures(): EditState {
    val syncedExport = apkExportConfig.withRuntimePermissionsSyncedFromFeatures(
        webViewConfig = webViewConfig,
        autoStartConfig = autoStartConfig,
        bgmEnabled = bgmEnabled
    )
    return if (syncedExport === apkExportConfig || syncedExport == apkExportConfig) {
        this
    } else {
        copy(apkExportConfig = syncedExport)
    }
}


/**
 * Applies the "browser toolbar" master toggle to a [WebViewConfig] (#654 redesign).
 *
 * Turning the toolbar on flips every item flag on — the user then trims individual
 * buttons from the all-on slate. Turning it off flips them all off: "off" means no
 * toolbar at all, so a re-enable must not resurrect stale per-item choices.
 */
fun WebViewConfig.withBrowserToolbarEnabled(enabled: Boolean): WebViewConfig = copy(
    browserToolbarEnabled = enabled,
    toolbarShowTitle = enabled,
    toolbarShowUrl = enabled,
    toolbarShowBack = enabled,
    toolbarShowForward = enabled,
    toolbarShowRefresh = enabled,
    toolbarShowConsole = enabled,
    toolbarShowFind = enabled
)

fun EditState.hasPreviewableContent(): Boolean = when (appType) {
    AppType.WEB -> url.isNotBlank()
    AppType.HTML, AppType.FRONTEND -> htmlConfig?.files?.isNotEmpty() == true
    AppType.IMAGE, AppType.VIDEO -> url.isNotBlank()
    else -> true
}
