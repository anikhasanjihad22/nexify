package com.ahr.nexify.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.ahr.nexify.core.shell.ShellConfig
import com.ahr.nexify.data.model.WebApp
import com.ahr.nexify.data.model.WebViewConfig
import com.ahr.nexify.data.model.WebViewConfig.Companion.BACK_EXIT
import com.ahr.nexify.data.model.WebViewConfig.Companion.BACK_GO_BACK
import com.ahr.nexify.data.model.WebViewConfig.Companion.BACK_GO_BACK_DIALOG
import com.ahr.nexify.data.model.WebViewConfig.Companion.BACK_GO_BACK_EXIT
import com.ahr.nexify.data.model.WebViewConfig.Companion.BACK_JS
import com.ahr.nexify.util.GsonProvider
import org.junit.Test

/**
 * Guards the Back Button Behavior config chain (issue #151): editor model -> ApkConfig
 * export block -> shell config JSON -> runtime ShellConfig. The runtime back handler reads
 * this from ShellConfig, so a break here silently reverts generated apps to history-back.
 */
class BackButtonBehaviorExportWiringTest {

    private fun roundTrip(app: WebApp): ShellConfig {
        val apk = app.toApkConfig("com.example.test")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        return GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
    }

    @Test
    fun `exit back behavior flows from model through ApkConfig into shell config`() {
        val app = WebApp(
            name = "Back",
            url = "https://example.com",
            webViewConfig = WebViewConfig(backButtonBehavior = BACK_EXIT)
        )

        val config = app.toApkConfig("com.example.back")
        assertThat(config.webView.backButtonBehavior).isEqualTo(BACK_EXIT)
        assertThat(roundTrip(app).webViewConfig.backButtonBehavior).isEqualTo(BACK_EXIT)
    }

    @Test
    fun `exit at first page flows from model through ApkConfig into shell config`() {
        val app = WebApp(
            name = "Root",
            url = "https://example.com",
            webViewConfig = WebViewConfig(backButtonBehavior = BACK_GO_BACK_EXIT)
        )

        val config = app.toApkConfig("com.example.root")
        assertThat(config.webView.backButtonBehavior).isEqualTo("GO_BACK_EXIT")
        assertThat(roundTrip(app).webViewConfig.backButtonBehavior).isEqualTo(BACK_GO_BACK_EXIT)
    }

    @Test
    fun `js back and volume switches survive export`() {
        val app = WebApp(
            name = "Keys",
            url = "https://example.com",
            webViewConfig = WebViewConfig(
                backButtonBehavior = BACK_JS,
                forwardVolumeKeys = true,
                consumeVolumeKeys = true
            )
        )

        val config = app.toApkConfig("com.example.keys")
        assertThat(config.webView.backButtonBehavior).isEqualTo(BACK_JS)
        assertThat(config.webView.forwardVolumeKeys).isTrue()
        assertThat(config.webView.consumeVolumeKeys).isTrue()
        val shell = roundTrip(app).webViewConfig
        assertThat(shell.backButtonBehavior).isEqualTo(BACK_JS)
        assertThat(shell.forwardVolumeKeys).isTrue()
        assertThat(shell.consumeVolumeKeys).isTrue()
    }

    @Test
    fun `back behavior defaults to go-back`() {
        val app = WebApp(name = "Default", url = "https://example.com")

        assertThat(app.toApkConfig("com.example.default").webView.backButtonBehavior).isEqualTo(BACK_GO_BACK)
        assertThat(roundTrip(app).webViewConfig.backButtonBehavior).isEqualTo(BACK_GO_BACK)
    }

    @Test
    fun `ask before exit flows from model through ApkConfig into shell config`() {
        val app = WebApp(
            name = "Ask",
            url = "https://example.com",
            webViewConfig = WebViewConfig(backButtonBehavior = BACK_GO_BACK_DIALOG)
        )

        assertThat(app.toApkConfig("com.example.ask").webView.backButtonBehavior).isEqualTo("GO_BACK_DIALOG")
        assertThat(roundTrip(app).webViewConfig.backButtonBehavior).isEqualTo(BACK_GO_BACK_DIALOG)
    }

    @Test
    fun `secure screen flows from model through ApkConfig into shell config`() {
        val app = WebApp(
            name = "Secure",
            url = "https://example.com",
            webViewConfig = WebViewConfig(secureScreen = true)
        )

        assertThat(app.toApkConfig("com.example.secure").webView.secureScreen).isTrue()
        assertThat(roundTrip(app).webViewConfig.secureScreen).isTrue()
    }

    @Test
    fun `secure screen is off by default`() {
        val app = WebApp(name = "Open", url = "https://example.com")

        assertThat(roundTrip(app).webViewConfig.secureScreen).isFalse()
    }
}
