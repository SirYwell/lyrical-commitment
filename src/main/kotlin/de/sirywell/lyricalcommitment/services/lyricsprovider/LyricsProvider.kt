package de.sirywell.lyricalcommitment.services.lyricsprovider

interface LyricsProvider {

    fun songLineAt(song: String, author: String = "", album: String = "", seconds: Int): String?
}
