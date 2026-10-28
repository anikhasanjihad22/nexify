package com.ahr.nexify.ui.screens

import android.net.Uri
import androidx.compose.runtime.Stable
import com.ahr.nexify.core.activation.ActivationCode
import com.ahr.nexify.core.appmodifier.AppModifyConfig
import com.ahr.nexify.core.appmodifier.InstalledAppInfo
import com.ahr.nexify.data.model.ActivationDialogConfig
import com.ahr.nexify.data.model.Announcement
import com.ahr.nexify.data.model.SplashConfig

@Stable
data class AppModifierEditState(
    val newAppName: String = "",
    val newIconUri: Uri? = null,
    val newIconPath: String? = null,

    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),

    val splashMediaUri: Uri? = null,

    val activationEnabled: Boolean = false,
    val activationCodes: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.ahr.nexify.data.model.RemoteActivationConfig = com.ahr.nexify.data.model.RemoteActivationConfig(),

    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement()
) {
    fun toConfig(originalApp: InstalledAppInfo): AppModifyConfig {
        return AppModifyConfig(
            originalApp = originalApp,
            newAppName = newAppName,
            newIconPath = newIconPath ?: newIconUri?.toString(),
            splashEnabled = splashEnabled && !splashConfig.mediaPath.isNullOrBlank(),
            splashConfig = splashConfig,
            activationEnabled = activationEnabled,
            activationCodes = activationCodes,
            activationRequireEveryTime = activationRequireEveryTime,
            activationDialogConfig = activationDialogConfig,
            activationRemoteConfig = activationRemoteConfig,
            announcementEnabled = announcementEnabled,
            announcement = announcement
        )
    }
}
