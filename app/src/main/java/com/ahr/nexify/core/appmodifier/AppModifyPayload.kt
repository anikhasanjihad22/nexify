package com.ahr.nexify.core.appmodifier

import com.ahr.nexify.core.activation.ActivationCode
import com.ahr.nexify.data.model.ActivationDialogConfig
import com.ahr.nexify.data.model.Announcement
import com.ahr.nexify.data.model.SplashConfig
import com.ahr.nexify.util.GsonProvider

data class AppModifyPayload(
    val targetPackage: String,
    val splashEnabled: Boolean = false,
    val splashConfig: SplashConfig = SplashConfig(),
    val activationEnabled: Boolean = false,
    val activationCodes: List<ActivationCode> = emptyList(),
    val activationRequireEveryTime: Boolean = false,
    val activationDialogConfig: ActivationDialogConfig = ActivationDialogConfig(),
    val activationRemoteConfig: com.ahr.nexify.data.model.RemoteActivationConfig = com.ahr.nexify.data.model.RemoteActivationConfig(),
    val announcementEnabled: Boolean = false,
    val announcement: Announcement = Announcement()
) {

    fun toJson(): String = GsonProvider.gson.toJson(this)

    fun needsLauncher(): Boolean {
        val hasSplash = splashEnabled && !splashConfig.mediaPath.isNullOrBlank()
        val hasActivation = activationEnabled &&
            (activationCodes.isNotEmpty() || activationRemoteConfig.enabled)
        val hasAnnouncement = announcementEnabled && announcement.title.isNotBlank()
        return hasSplash || hasActivation || hasAnnouncement
    }

    companion object {
        fun fromJson(json: String?): AppModifyPayload? {
            if (json.isNullOrBlank()) return null
            return try {
                GsonProvider.gson.fromJson(json, AppModifyPayload::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}
