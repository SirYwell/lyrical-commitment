package de.sirywell.lyricalcommitment.services

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying
import java.net.URI

class SpotifyService {
    companion object {
        private const val clientId = "6cc537455793437abf0b6e887137accd"

        @JvmStatic
        val state = loadState()

        private fun loadState(): SpotifyServiceState {
            return SpotifyServiceState()
        }

        const val maxCodeLength = 128

        const val minCodeLength = 43

        const val port = 51258

        val spotifyApi: SpotifyApi = SpotifyApi.builder()
            .setRedirectUri(URI.create("http://localhost:$port/login"))
            .setClientId(clientId)
            .build()

        fun setAccessToken(accessToken: String?) {
            spotifyApi.accessToken = accessToken
            state.accessToken = accessToken
        }

        fun setRefreshToken(refreshToken: String?) {
            spotifyApi.refreshToken = refreshToken
            state.refreshToken = refreshToken
        }
    }

    fun getCurrentSongInfo(): CurrentlyPlaying? {
        return spotifyApi.usersCurrentlyPlayingTrack.build().execute()
    }

    fun ready() = !spotifyApi.accessToken.isNullOrBlank()

    class SpotifyServiceState(
        var accessToken: String? = null,
        var refreshToken: String? = null
    ) {

        fun loggedIn() = accessToken != null && refreshToken != null
    }
}
