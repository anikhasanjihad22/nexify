package com.ahr.nexify.core.appmodifier

import android.graphics.drawable.Drawable
import com.ahr.nexify.core.activation.ActivationCode
import com.ahr.nexify.data.model.ActivationDialogConfig
import com.ahr.nexify.data.model.Announcement
import com.ahr.nexify.data.model.SplashConfig

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val icon: Drawable?,
    val apkPath: String,
    val isSystemApp: Boolean,
    val installedTime: Long,
    val updatedTime: Long,
    val apkSize: Long
) {

    val formattedSize: String
        get() {
            val kb = apkSize / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1 -> String.format(java.util.Locale.getDefault(), "%.1f MB", mb)
                kb >= 1 -> String.format(java.util.Locale.getDefault(), "%.1f KB", kb)
                else -> "$apkSize B"
            }
        }
}

data class AppModifyConfig(
    val originalApp: InstalledAppInfo,
    val newAppName: String,
    val newIconPath: String? = null,

    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),

    val activationEnabled: Boolean = false,
    val activationCodes: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.ahr.nexify.data.model.RemoteActivationConfig = com.ahr.nexify.data.model.RemoteActivationConfig(),

    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement()
)

sealed class AppModifyResult {

    data object ShortcutSuccess : AppModifyResult()

    data class CloneSuccess(val apkPath: String) : AppModifyResult()

    data class Error(val message: String) : AppModifyResult()
}

enum class AppFilterType {
    ALL,
    USER,
    SYSTEM
}
