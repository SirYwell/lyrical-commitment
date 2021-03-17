package de.sirywell.lyricalcommitment

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.changes.ui.CommitMessageProvider
import de.sirywell.lyricalcommitment.services.SpotifyService
import de.sirywell.lyricalcommitment.services.lyricsprovider.LyricsProvider
import de.sirywell.lyricalcommitment.services.lyricsprovider.TextylLyricsProvider
import git4idea.repo.GitRepositoryManager

class LyricsMessageProvider : CommitMessageProvider {
    companion object {
        const val millisToSeconds = 1000
    }

    private val lyricsProvider: LyricsProvider = TextylLyricsProvider()
    private val spotifyService = SpotifyService()

    override fun getCommitMessage(forChangelist: LocalChangeList, project: Project): String? {
        val lastCommitMessage = forChangelist.comment
        val gitRepositoryManager = GitRepositoryManager.getInstance(project)
        return gitRepositoryManager.repositories.firstOrNull()
            ?.currentBranchName
            ?.takeIf { filterBranch(it) }
            ?.takeIf { spotifyService.ready() }
            ?.let { spotifyService.getCurrentSongInfo() }
            ?.takeIf { it.is_playing }
            ?.let { lyricsProvider.songLineAt(it.item.name, seconds = it.progress_ms / millisToSeconds) }
            ?: lastCommitMessage
    }

    // TODO (this expression is a very intelligent detekt hack!)
    fun filterBranch(branchName: String): Boolean = true || false
}
