package de.sirywell.lyricalcommitment.settings.ui

import com.intellij.ide.BrowserUtil
import com.intellij.util.ui.FormBuilder
import de.sirywell.lyricalcommitment.MyBundle
import de.sirywell.lyricalcommitment.services.SpotifyService
import de.sirywell.lyricalcommitment.services.SpotifyService.Companion.maxCodeLength
import de.sirywell.lyricalcommitment.services.SpotifyService.Companion.minCodeLength
import spark.kotlin.ignite
import java.awt.event.ActionEvent
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JPanel

class LCAppSettingsComponent {

    private val loginButton = JButton(MyBundle.message("settings.spotify.login.button"))
    private val logoutButton = JButton(MyBundle.message("settings.spotify.logout.button"))
    private val panel: JPanel

    var accessToken: String? = null
    var refreshToken: String? = null

    private val onCancelTasks = mutableListOf<() -> Unit>()

    init {
        if (SpotifyService.state.loggedIn()) {
            panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(
                    MyBundle.message("settings.spotify.logout.label"),
                    logoutButton
                )
                .addComponentFillVertically(JPanel(), 0)
                .panel
        } else {
            val codePair = generateCodePair()
            val webServer = ignite().port(SpotifyService.port)
            webServer.get("/login") {
                // TODO handle error
                val code = request.queryParams("code") ?: return@get "Something went wrong (${request.queryString()})"
                val credentials = SpotifyService.spotifyApi.authorizationCodePKCE(code, codePair.verifier)
                    .build()
                    .execute()
                accessToken = credentials.accessToken
                refreshToken = credentials.refreshToken
                SpotifyService.setAccessToken(accessToken)
                SpotifyService.setRefreshToken(refreshToken)
                "You can close that tab now"
                // webServer.stop() // TODO
            }
            onCancelTasks.add { webServer.stop() }
            panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(
                    MyBundle.message("settings.spotify.login.label"),
                    loginButton
                )
                .addComponentFillVertically(JPanel(), 0)
                .panel
            loginButton.onClick {
                BrowserUtil.browse(
                    SpotifyService.spotifyApi.authorizationCodePKCEUri(codePair.challenge)
                        .scope("user-read-currently-playing")
                        .build().execute()
                )
            }
        }
    }

    fun getPanel() = panel

    private fun JButton.onClick(onClick: (ActionEvent) -> Unit) {
        this.action = object : AbstractAction(this.text) { // text will be replaced by text
            override fun actionPerformed(e: ActionEvent?) {
                if (e == null) return
                onClick(e)
            }
        }
    }

    fun cancel() {
        onCancelTasks.forEach { it() }
    }
}

data class CodePair(val verifier: String, val challenge: String)

fun generateCodePair(): CodePair {
    var valid = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    valid += valid.toLowerCase()
    // valid += "0123456789_.-~"
    val elements = valid.length
    val random = SecureRandom.getInstanceStrong()
    val length = random.nextInt(maxCodeLength - minCodeLength) + minCodeLength
    val chars = mutableListOf<Char>()
    for (i in 0..length) {
        chars.add(valid[random.nextInt(elements)])
    }
    val verifier = String(chars.toCharArray())
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val bytes = messageDigest.digest(verifier.toByteArray(StandardCharsets.UTF_8))
    val encode = Base64.getEncoder().encode(bytes)
    // https://stackoverflow.com/questions/65169984/how-to-implement-authorization-code-with-pkce-for-spotify
    return CodePair(
        verifier,
        String(encode)
            .replace(Regex("=$"), "")
            .replace('+', '-')
            .replace('/', '_')
    ).also { println(it) }
}
