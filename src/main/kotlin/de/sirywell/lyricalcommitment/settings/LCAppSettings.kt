package de.sirywell.lyricalcommitment.settings

import com.intellij.openapi.options.Configurable
import de.sirywell.lyricalcommitment.MyBundle
import de.sirywell.lyricalcommitment.services.SpotifyService
import de.sirywell.lyricalcommitment.settings.ui.LCAppSettingsComponent
import javax.swing.JComponent

class LCAppSettings : Configurable {
    private var settingsComponent: LCAppSettingsComponent? = null

    override fun createComponent(): JComponent {
        val component = LCAppSettingsComponent()
        settingsComponent = component
        return component.getPanel()
    }

    override fun isModified(): Boolean {
        return settingsComponent?.accessToken?.equals(SpotifyService.state.accessToken) ?: false &&
            settingsComponent?.refreshToken?.equals(SpotifyService.state.refreshToken) ?: false
    }

    override fun apply() {
        SpotifyService.state.accessToken = settingsComponent?.accessToken
        SpotifyService.state.refreshToken = settingsComponent?.refreshToken
    }

    override fun getDisplayName() = MyBundle.getMessage("name")

    override fun cancel() {
        super.cancel()
        settingsComponent?.cancel()
    }

    override fun disposeUIResources() {
        super.disposeUIResources()
        settingsComponent?.cancel()
    }
}
