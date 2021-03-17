package de.sirywell.lyricalcommitment

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.changes.ui.CommitMessageProvider
import de.sirywell.lyricalcommitment.services.SpotifyService
import de.sirywell.lyricalcommitment.services.lyricsprovider.LyricsProvider
import de.sirywell.lyricalcommitment.services.lyricsprovider.TextylLyricsProvider
import git4idea.repo.GitRepositoryManager

class LyricsMessageProvider : CommitMessageProvider {

    private val lyricsProvider: LyricsProvider = TextylLyricsProvider()
    private val spotifyService = SpotifyService()

    override fun getCommitMessage(forChangelist: LocalChangeList, project: Project): String? {
        val lastCommitMessage = forChangelist.comment
        val gitRepositoryManager = GitRepositoryManager.getInstance(project)
        val s = gitRepositoryManager.repositories.firstOrNull()?.currentBranchName ?: return lastCommitMessage
        if (!filterBranch(s)) return lastCommitMessage
        val currentSongInfo = spotifyService.getCurrentSongInfo()
        if (currentSongInfo?.is_playing != true) return lastCommitMessage
        return lyricsProvider.songLineAt(currentSongInfo.item.name, seconds = currentSongInfo.progress_ms / 1000)
            ?: lastCommitMessage
    }

    fun filterBranch(branchName: String): Boolean = true // TODO
}
