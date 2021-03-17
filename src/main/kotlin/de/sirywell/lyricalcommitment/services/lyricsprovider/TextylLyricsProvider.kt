package de.sirywell.lyricalcommitment.services.lyricsprovider

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.time.Duration

class TextylLyricsProvider : LyricsProvider {
    private val gson = GsonBuilder().create()
    private val baseUri = "https://api.textyl.co/api/lyrics?q="
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(2))
        .build()

    private var last: Lyrics? = null

    override fun songLineAt(song: String, author: String, album: String, seconds: Int): String? {
        if (song == last?.song) { // very simple cache
            return last?.findBySeconds(seconds)
        }
        val request = HttpRequest.newBuilder()
            .uri(java.net.URI.create("$baseUri${URLEncoder.encode(song, Charset.defaultCharset())}"))
            .GET()
            .timeout(Duration.ofSeconds(2))
            .build()
        return try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString()).run {
                val list: List<Line> = gson.fromJson<List<Line>>(body())
                val lyrics = Lyrics(song, list)
                last = lyrics
                lyrics.findBySeconds(seconds)
            }
        } catch (ex: IOException) {
            null
        } catch (ex: JsonSyntaxException) {
            null
        }
    }

    data class Lyrics(val song: String, val lines: List<Line>) {

        fun findBySeconds(seconds: Int): String? {
            var prev: String? = null
            for ((s, l) in lines) {
                if (s > seconds) return prev
                prev = l
            }
            return prev
        }
    }

    data class Line(val seconds: Int, val lyrics: String)

    inline fun <reified T> Gson.fromJson(json: String) = fromJson<T>(json, object : TypeToken<T>() {}.type)
}
