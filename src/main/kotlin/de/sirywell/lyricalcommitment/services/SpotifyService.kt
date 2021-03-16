package de.sirywell.lyricalcommitment.services

import com.wrapper.spotify.SpotifyApi
import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlaying

class SpotifyService {

    private val spotifyApi = SpotifyApi.builder()
        // TODO
        .setAccessToken("")
        .build()

    fun getCurrentSongInfo(): CurrentlyPlaying {
        return spotifyApi.usersCurrentlyPlayingTrack.build().execute()
    }


}