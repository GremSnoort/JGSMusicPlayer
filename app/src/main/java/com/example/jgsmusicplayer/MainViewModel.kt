package com.example.jgsmusicplayer

import android.app.Application
import android.content.ComponentName

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken

import com.example.jgsmusicplayer.data.AudioRepository
import com.example.jgsmusicplayer.model.AudioFile
import com.example.jgsmusicplayer.model.PlayerUiState
import com.example.jgsmusicplayer.playback.PlaybackService
import com.google.common.util.concurrent.ListenableFuture

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AudioRepository(application.applicationContext)

    var uiState by mutableStateOf(PlayerUiState())
        private set

    var files by mutableStateOf<List<AudioFile>>(emptyList())
        private set

    private var mediaController: MediaController? = null
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var refreshJob: Job? = null
    private var progressJob: Job? = null
    private var pendingTrackToPlay: AudioFile? = null

    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncPlayerState(player)
        }

        override fun onPlayerError(error: PlaybackException) {
            uiState = uiState.copy(
                isPlaying = false,
                errorMessage = error.message ?: "Playback error"
            )
        }
    }

    init {
        connectToPlaybackService()
    }

    fun refreshAudioFiles() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            files = repository.loadAudioFiles()
            mediaController?.let(::syncPlayerState)
        }
    }

    fun clearPlaybackError() {
        if (uiState.errorMessage != null) {
            uiState = uiState.copy(errorMessage = null)
        }
    }

    fun playPause() {
        val controller = mediaController ?: return
        val currentTrack = uiState.nowPlaying ?: pendingTrackToPlay

        if (controller.mediaItemCount == 0) {
            if (currentTrack == null) return

            controller.setMediaItem(buildMediaItem(currentTrack))
            controller.prepare()
            controller.play()
            uiState = uiState.copy(
                nowPlaying = currentTrack,
                isPlaying = true,
                errorMessage = null
            )
            pendingTrackToPlay = null
            return
        }

        when {
            controller.playbackState == Player.STATE_ENDED -> {
                controller.seekTo(0)
                controller.play()
                uiState = uiState.copy(
                    isPlaying = true,
                    positionMs = 0L,
                    errorMessage = null
                )
            }

            controller.isPlaying -> {
                controller.pause()
                uiState = uiState.copy(isPlaying = false)
            }

            else -> {
                controller.play()
                uiState = uiState.copy(isPlaying = true, errorMessage = null)
            }
        }
    }

    fun stopPlayback() {
        mediaController?.repeatMode = Player.REPEAT_MODE_OFF
        mediaController?.stop()
        mediaController?.clearMediaItems()
        pendingTrackToPlay = null
        uiState = uiState.copy(
            nowPlaying = null,
            isPlaying = false,
            isLooping = false,
            durationMs = 0L,
            positionMs = 0L,
            errorMessage = null
        )
    }

    fun playTrack(file: AudioFile) {
        val controller = mediaController
        if (controller == null) {
            pendingTrackToPlay = file
            uiState = uiState.copy(
                nowPlaying = file,
                isPlaying = false,
                errorMessage = null
            )
            return
        }

        val mediaItem = buildMediaItem(file)

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
        pendingTrackToPlay = null

        uiState = uiState.copy(
            nowPlaying = file,
            isPlaying = true,
            errorMessage = null
        )
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs.coerceAtLeast(0L))
    }

    fun seekBy(deltaMs: Long) {
        val controller = mediaController ?: return
        val duration = uiState.durationMs.takeIf { it > 0 } ?: controller.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        val target = (controller.currentPosition + deltaMs).coerceIn(0L, duration)
        controller.seekTo(target)
    }

    fun toggleLooping() {
        val controller = mediaController ?: return
        controller.repeatMode = if (controller.repeatMode == Player.REPEAT_MODE_ONE) {
            Player.REPEAT_MODE_OFF
        } else {
            Player.REPEAT_MODE_ONE
        }
    }

    private fun connectToPlaybackService() {
        val context = getApplication<Application>().applicationContext
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture = future

        future.addListener(
            {
                runCatching { future.get() }
                    .onSuccess { controller ->
                        mediaController = controller
                        controller.addListener(playerListener)
                        startProgressUpdates()
                        pendingTrackToPlay?.let { playTrack(it) }
                        syncPlayerState(controller)
                    }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun syncPlayerState(player: Player) {
        val mediaItemUri = player.currentMediaItem?.localConfiguration?.uri
        val placeholderTrack = mediaItemUri?.let { uri ->
            AudioFile(
                name = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "Unknown track",
                folder = uiState.nowPlaying?.folder.orEmpty(),
                uri = uri
            )
        }
        val fallbackTrack = uiState.nowPlaying?.takeIf { current -> current.uri == mediaItemUri } ?: placeholderTrack
        val activeTrack = files.firstOrNull { it.uri == mediaItemUri } ?: fallbackTrack

        uiState = uiState.copy(
            nowPlaying = activeTrack,
            isPlaying = player.isPlaying,
            isLooping = player.repeatMode == Player.REPEAT_MODE_ONE,
            durationMs = player.duration.takeIf { it > 0 } ?: 0L,
            positionMs = player.currentPosition.coerceAtLeast(0L),
            errorMessage = uiState.errorMessage
        )
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    val updatedPosition = controller.currentPosition.coerceAtLeast(0L)
                    val updatedDuration = controller.duration.takeIf { it > 0 } ?: 0L
                    if (uiState.positionMs != updatedPosition || uiState.durationMs != updatedDuration) {
                        uiState = uiState.copy(
                            positionMs = updatedPosition,
                            durationMs = updatedDuration
                        )
                    }
                }

                delay(250)
            }
        }
    }

    override fun onCleared() {
        refreshJob?.cancel()
        progressJob?.cancel()
        mediaController?.removeListener(playerListener)
        mediaController?.release()
        mediaController = null
        mediaControllerFuture?.cancel(false)
        mediaControllerFuture = null
        super.onCleared()
    }

    private fun buildMediaItem(file: AudioFile): MediaItem {
        return MediaItem.Builder()
            .setMediaId(file.uri.toString())
            .setUri(file.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(file.name)
                    .build()
            )
            .build()
    }
}
